package com.hltech.judged.server.domain;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository;
import com.hltech.judged.server.domain.validation.EnvironmentValidatorResult;
import com.hltech.judged.server.domain.validation.InterfaceContractValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
public class JudgeDApplicationService {

    private EnvironmentRepository environmentRepository;
    private ServiceContractsRepository serviceContractsRepository;

    @Autowired
    public JudgeDApplicationService(EnvironmentRepository environmentRepository, ServiceContractsRepository serviceContractsRepository) {
        this.environmentRepository = environmentRepository;
        this.serviceContractsRepository = serviceContractsRepository;
    }

    public <C, E> EnvironmentValidatorResult validateServiceAgainstEnvironments(
        ServiceContracts contractsToValidate,
        List<String> environments,
        InterfaceContractValidator<C, E> validator
    ) {
        List<ServiceContracts> environmentContracts = environments.stream()
            .flatMap(env -> this.environmentRepository.get(env).getAllServices().stream())
            .map(sv -> this.serviceContractsRepository.findOne(sv))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        return getValidatorResult(contractsToValidate, environmentContracts, validator);
    }

    public <C, E> Map<ServiceVersion, EnvironmentValidatorResult> validatedServicesAgainstEnvironment(
        List<ServiceContracts> contractToValidate,
        String env,
        InterfaceContractValidator<C, E> validator
    ){
        // find contracts on given env
        List<ServiceContracts> environmentContracts = this.environmentRepository.get(env).getAllServices()
            .stream()
            .map(sv -> this.serviceContractsRepository.findOne(sv))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        log.info("checking how " +
            "["+ Joiner.on(", ").join(contractToValidate.stream().map(sc -> sc.getId().toString()).collect(toList()))+"] will impact the env " +
            "["+ Joiner.on(", ").join(environmentContracts.stream().map(sc -> sc.getId().toString()).collect(toList()))+"]");

        // replace environment contracts with validated ones
        contractToValidate.forEach(validatedContract -> {
            Iterator<ServiceContracts> iterator = environmentContracts.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getName().equals(validatedContract.getId().getName())) {
                    iterator.remove();
                }
            }
            environmentContracts.add(validatedContract);
        });

        log.info("after the deployment env will contain: ["+Joiner.on(", ").join(environmentContracts.stream().map(sc -> sc.getId().toString()).collect(toList()))+"]");

        Map<ServiceVersion, EnvironmentValidatorResult> hashMap = Maps.newHashMap();
        for (ServiceContracts sc : contractToValidate) {
            hashMap.put(sc.getId(), getValidatorResult(sc, environmentContracts, validator));
        }
        return hashMap;
    }

    public <C, E> EnvironmentValidatorResult getValidatorResult(
        ServiceContracts validatedService,
        Collection<ServiceContracts> environmentContracts,
        InterfaceContractValidator<C, E> validator
    ) {
        return new EnvironmentValidatorResult(
            validator.getCommunicationInterface(),
            validator.validateCapabilities(validatedService, environmentContracts),
            validator.validateExpectations(validatedService, environmentContracts)
        );
    }
}

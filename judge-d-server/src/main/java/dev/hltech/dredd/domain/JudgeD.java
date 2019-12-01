package dev.hltech.dredd.domain;

import com.google.common.collect.Maps;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class JudgeD {

    private EnvironmentRepository environmentRepository;
    private ServiceContractsRepository serviceContractsRepository;

    @Autowired
    public JudgeD(EnvironmentRepository environmentRepository, ServiceContractsRepository serviceContractsRepository) {
        this.environmentRepository = environmentRepository;
        this.serviceContractsRepository = serviceContractsRepository;
    }

    public <C, E> EnvironmentValidatorResult validateServiceAgainstEnvironments(
        ServiceContracts validatedService,
        List<String> environments,
        InterfaceContractValidator<C, E> validator
    ) {
        List<ServiceContracts> environmentContracts = environments.stream()
            .flatMap(env -> this.environmentRepository.get(env).getAllServices().stream())
            .map(sv -> this.serviceContractsRepository.findOne(sv))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        return getValidatorResult(validatedService, environmentContracts, validator);
    }

    public <C, E> Map<ServiceVersion, EnvironmentValidatorResult> validatedServicesAgainstEnvironment(
        List<ServiceContracts> validatedContracts,
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

        // replace environment contracts with validated ones
        validatedContracts.forEach(validatedContract -> {
            Iterator<ServiceContracts> iterator = environmentContracts.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getName().equals(validatedContract.getId().getName())) {
                    iterator.remove();
                }
            }
            environmentContracts.add(validatedContract);
        });

        Map<ServiceVersion, EnvironmentValidatorResult> hashMap = Maps.newHashMap();
        for (ServiceContracts sc : validatedContracts) {
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

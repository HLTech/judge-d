package com.hltech.judged.server.domain;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hltech.judged.server.domain.environment.Environment;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository;
import com.hltech.judged.server.domain.environment.Space;
import com.hltech.judged.server.domain.validation.EnvironmentValidatorResult;
import com.hltech.judged.server.domain.validation.InterfaceContractValidator;
import com.hltech.judged.server.interfaces.rest.RequestValidationException;
import com.hltech.judged.server.interfaces.rest.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.hltech.judged.server.domain.environment.Environment.DEFAULT_NAMESPACE;
import static com.hltech.judged.server.domain.validation.EnvironmentValidatorResult.getValidatorResult;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class JudgeDApplicationService {

    private final EnvironmentRepository environmentRepository;
    private final ServiceContractsRepository serviceContractsRepository;
    private final List<InterfaceContractValidator<?, ?>> validators;

    public void overwriteEnvironment(String environmentName, String agentSpace, Set<ServiceId> serviceIds) {
        String space = firstNonNull(agentSpace, DEFAULT_NAMESPACE);

        Environment environment = environmentRepository.get(environmentName);
        Set<String> supportedSpaces = ImmutableSet.<String>builder()
            .addAll(environment.getSpaceNames())
            .add(space)
            .build();

        Set<Space> spaces = new HashSet<>();
        for (String spaceName : supportedSpaces) {
            if (space.equals(spaceName)) {
                spaces.add(new Space(spaceName, serviceIds));
            } else {
                spaces.add(new Space(spaceName, environment.getServices(spaceName)));
            }
        }

        environmentRepository.persist(new Environment(environmentName, spaces));
    }

    public Collection<EnvironmentValidatorResult> validateServiceAgainstEnvironments(
        ServiceId serviceId,
        List<String> environments) {

        ServiceContracts validatedServiceContracts = this.serviceContractsRepository.findOne(serviceId)
            .orElseThrow(ResourceNotFoundException::new);

        return this.validators.stream()
            .map(validator ->
                validateServiceAgainstEnvironments(
                    validatedServiceContracts,
                    environments,
                    validator
                ))
            .collect(toList());
    }

    public Multimap<ServiceId, EnvironmentValidatorResult> validatedServicesAgainstEnvironment(
        List<ServiceId> serviceIds,
        String environment) {

        List<ServiceContracts> validatedServiceContracts = serviceIds.stream()
            .map(serviceContractsRepository::findOne)
            .map(o -> o.orElseThrow(RequestValidationException::new))
            .collect(toList());

        Multimap<ServiceId, EnvironmentValidatorResult> validationResults = HashMultimap.create();
        this.validators
            .forEach(validator ->
                validatedServicesAgainstEnvironment(
                    validatedServiceContracts,
                    environment,
                    validator
                )
                    .forEach(validationResults::put)
            );

        return validationResults;
    }

    private <C, E> EnvironmentValidatorResult validateServiceAgainstEnvironments(
        ServiceContracts contractsToValidate,
        List<String> environments,
        InterfaceContractValidator<C, E> validator
    ) {
        List<ServiceContracts> environmentContracts = environments.stream()
            .flatMap(env -> this.environmentRepository.get(env).getAllServices().stream())
            .map(service -> this.serviceContractsRepository.findOne(new ServiceId(service.getName(), service.getVersion())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        return getValidatorResult(contractsToValidate, environmentContracts, validator);
    }

    private <C, E> Map<ServiceId, EnvironmentValidatorResult> validatedServicesAgainstEnvironment(
        List<ServiceContracts> contractToValidate,
        String env,
        InterfaceContractValidator<C, E> validator
    ){
        // find contracts on given env
        List<ServiceContracts> environmentContracts = this.environmentRepository.get(env).getAllServices()
            .stream()
            .map(service -> this.serviceContractsRepository.findOne(new ServiceId(service.getName(), service.getVersion())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        log.info("checking how " +
            "["+ Joiner.on(", ").join(contractToValidate.stream().map(sc -> sc.getId().toString()).collect(toList()))+"] will impact the env " +
            "["+ Joiner.on(", ").join(environmentContracts.stream().map(sc -> sc.getId().toString()).collect(toList()))+"]");

        // replace environment contracts with validated ones
        contractToValidate.forEach(validatedContract -> {
            environmentContracts.removeIf(serviceContracts -> serviceContracts.getName().equals(validatedContract.getId().getName()));
            environmentContracts.add(validatedContract);
        });

        log.info("after the deployment env will contain: ["+Joiner.on(", ").join(environmentContracts.stream().map(sc -> sc.getId().toString()).collect(toList()))+"]");

        Map<ServiceId, EnvironmentValidatorResult> hashMap = Maps.newHashMap();
        for (ServiceContracts sc : contractToValidate) {
            hashMap.put(sc.getId(), getValidatorResult(sc, environmentContracts, validator));
        }
        return hashMap;
    }
}

package dev.hltech.dredd.domain;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class JudgeD {

    private EnvironmentRepository environmentRepository;
    private ServiceContractsRepository serviceContractsRepository;

    @Autowired
    public JudgeD(EnvironmentRepository environmentRepository, ServiceContractsRepository serviceContractsRepository) {
        this.environmentRepository = environmentRepository;
        this.serviceContractsRepository = serviceContractsRepository;
    }

    public <C, E> EnvironmentValidatorResult validateServiceAgainstEnvironments(ServiceContracts validatedService, List<String> environments, InterfaceContractValidator<C, E> validator) {

        List<ServiceContracts> environmentContracts = environments.stream()
            .flatMap(env -> this.environmentRepository.get(env).getAllServices().stream())
            .map(sv -> this.serviceContractsRepository.find(sv.getName(), sv.getVersion()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        return new EnvironmentValidatorResult(
            validator.getCommunicationInterface(),
            validator.validateCapabilities(validatedService, environmentContracts),
            validator.validateExpectations(validatedService, environmentContracts)
        );
    }
}

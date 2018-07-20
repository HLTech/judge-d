package dev.hltech.dredd.domain;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.*;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.environment.EnvironmentAggregate.ServiceVersion;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InteractionValidationReport.InteractionValidationResult.FAILED;
import static java.util.stream.Collectors.toList;


public class Dredd {

    public static final InteractionValidationReport INTERACTION_VALIDATION_REPORT_4_NO_PROVIDER = new InteractionValidationReport(
        "any",
        FAILED,
        newArrayList("provider not registered")
    );
    private final ServiceContractsRepository serviceContractsRepository;
    private final EnvironmentRepository environmentRepository;

    public Dredd(ServiceContractsRepository serviceContractsRepository, EnvironmentRepository environmentRepository){
        this.serviceContractsRepository = serviceContractsRepository;
        this.environmentRepository = environmentRepository;
    }

    public List<CapabilitiesValidationResult> validate(String environment, CapabilitiesValidator capabilities) {
        Set<ServiceContracts> allRegisteredServices = environmentRepository.get(environment).getAllServices()
            .stream()
            .map(sv -> serviceContractsRepository.find(sv.getName(), sv.getVersion()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());

        return allRegisteredServices.stream()
            .map(capabilities::validate)
            .filter(result -> !result.isEmpty())
            .collect(toList());
    }

    public List<ExpectationValidationResult> validate(String environment, String providerName, ExpectationsValidator expectations) throws ProviderNotAvailableException {
        Collection<ServiceVersion> providersOnEnv = environmentRepository
            .get(environment)
            .findServices(providerName);

        if (providersOnEnv.isEmpty())
            throw new ProviderNotAvailableException();

        return providersOnEnv
            .stream()
            .map(provider -> validate(provider, expectations))
            .collect(toList());
    }

    private ExpectationValidationResult validate(ServiceVersion provider, ExpectationsValidator expectations) {
        Optional<ServiceContracts> serviceAggregate = serviceContractsRepository.find(provider.getName(), provider.getVersion());

        if (serviceAggregate.isPresent()) {
            return expectations.validate(serviceAggregate.get());
        } else {
            return new ExpectationValidationResult(
                provider.getName(),
                provider.getVersion(),
                newArrayList(INTERACTION_VALIDATION_REPORT_4_NO_PROVIDER)
            );
        }
    }

}

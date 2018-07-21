package dev.hltech.dredd.domain.validation;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.environment.EnvironmentAggregate;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
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

    public <C, E> ContractValidator<C, E> createContractValidator(String environment, InterfaceContractValidator<C, E> interfaceContractValidator) {
        return new ContractValidator(environment, interfaceContractValidator);
    }

    public class ContractValidator<C, E> {

        public final InterfaceContractValidator.InteractionValidationResult INTERACTION_VALIDATION_REPORT_4_NO_PROVIDER = new InterfaceContractValidator.InteractionValidationResult(
            "any",
            FAILED,
            newArrayList("provider not registered")
        );

        private String environment;
        private InterfaceContractValidator<C, E> interfaceContractValidator;

        public ContractValidator(String environment, InterfaceContractValidator<C, E> interfaceContractValidator) {
            this.environment = environment;
            this.interfaceContractValidator = interfaceContractValidator;
        }

        public List<InterfaceContractValidator.CapabilitiesValidationResult> validateCapabilities(String providerName, C capabilities) {
            Set<ServiceContracts> allRegisteredServices = environmentRepository.get(environment).getAllServices()
                .stream()
                .map(sv -> serviceContractsRepository.find(sv.getName(), sv.getVersion()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

            return allRegisteredServices.stream()
                .map(consumer -> interfaceContractValidator.validate(consumer, providerName, capabilities))
                .filter(result -> !result.isEmpty())
                .collect(toList());
        }

        public List<InterfaceContractValidator.ExpectationValidationResult> validateExpectations(String providerName, E expectations) throws ProviderNotAvailableException {
            Collection<EnvironmentAggregate.ServiceVersion> providersOnEnv = environmentRepository
                .get(environment)
                .findServices(providerName);

            if (providersOnEnv.isEmpty())
                throw new ProviderNotAvailableException();

            return providersOnEnv
                .stream()
                .map(provider -> {
                    Optional<ServiceContracts> osc = serviceContractsRepository.find(provider.getName(), provider.getVersion());
                    if (osc.isPresent()) {
                        return interfaceContractValidator.validate(osc.get(), expectations);
                    } else {
                        return new InterfaceContractValidator.ExpectationValidationResult(
                            provider.getName(),
                            provider.getVersion(),
                            newArrayList(INTERACTION_VALIDATION_REPORT_4_NO_PROVIDER)
                        );
                    }
                })
                .collect(toList());
        }
    }


}

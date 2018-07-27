package dev.hltech.dredd.domain.validation;

import com.google.common.collect.Lists;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.environment.EnvironmentAggregate;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator.ExpectationValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.CapabilitiesValidationResult;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
import static java.util.Collections.emptyList;
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

        public List<CapabilitiesValidationResult> validateCapabilitiee(ServiceContracts serviceContractsName) {
            return this.interfaceContractValidator
                .getCapabilities(serviceContractsName)
                .map(capabilities -> validateCapabilities(serviceContractsName.getName(), capabilities))
                .orElse(emptyList());
        }

        public List<ExpectationValidationResult> validateExpectations(ServiceContracts serviceContracts) {
            return this.interfaceContractValidator
                .getExpectations(serviceContracts)
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return validateExpectations(entry.getKey(), entry.getValue());
                    } catch (ProviderNotAvailableException e) {
                        return Lists.<ExpectationValidationResult>newArrayList();
                    }
                })
                .flatMap(List::stream)
                .collect(toList());
        }

        public List<CapabilitiesValidationResult> validateCapabilities(String providerName, C capabilities) {
            return getAllServiceContracts(this.environment)
                .stream()
                .map(potentialConsumer -> this.interfaceContractValidator.validate(potentialConsumer, providerName, capabilities))
                .filter(result -> !result.isEmpty())
                .collect(toList());
        }

        public List<InterfaceContractValidator.ExpectationValidationResult> validateExpectations(String providerName, E expectations) throws ProviderNotAvailableException {
            Collection<EnvironmentAggregate.ServiceVersion> providersOnEnv = JudgeD.this.environmentRepository
                .get(this.environment)
                .findServices(providerName);

            if (providersOnEnv.isEmpty())
                throw new ProviderNotAvailableException();

            return providersOnEnv
                .stream()
                .map(provider -> {
                    Optional<ServiceContracts> osc = JudgeD.this.serviceContractsRepository.find(provider.getName(), provider.getVersion());
                    if (osc.isPresent()) {
                        return this.interfaceContractValidator.validate(osc.get(), expectations);
                    } else {
                        return new InterfaceContractValidator.ExpectationValidationResult(
                            provider.getName(),
                            provider.getVersion(),
                            newArrayList(this.INTERACTION_VALIDATION_REPORT_4_NO_PROVIDER)
                        );
                    }
                })
                .collect(toList());
        }

        private Set<ServiceContracts> getAllServiceContracts(String environment) {
            return JudgeD.this.environmentRepository.get(environment).getAllServices()
                .stream()
                .map(sv -> JudgeD.this.serviceContractsRepository.find(sv.getName(), sv.getVersion()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        }
    }


}

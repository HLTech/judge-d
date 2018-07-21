package dev.hltech.dredd.domain.validation;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
import static java.util.Collections.emptyList;

public abstract class InterfaceContractValidator<C, E> {

    private String communicationInterface;

    public InterfaceContractValidator(String communicationInterface) {
        this.communicationInterface = communicationInterface;
    }

    public CapabilitiesValidationResult validate(ServiceContracts consumer, String providerName, C capabilities) {
        List<InteractionValidationResult> validatedInteractions = consumer.getExpectations(providerName, communicationInterface)
            .map(expectations -> validate(asExpectations(expectations), capabilities))
            .orElse(emptyList());

        return new CapabilitiesValidationResult(
            consumer.getName(),
            consumer.getVersion(),
            validatedInteractions
        );
    }

    public ExpectationValidationResult validate(ServiceContracts provider, E expectations) {
        List<InteractionValidationResult> validatedInteractions = provider.getCapabilities(communicationInterface)
            .map(capabilities -> validate(expectations, asCapabilities(capabilities)))
            .orElseGet(() -> newArrayList(
                new InteractionValidationResult(
                    "any",
                    FAILED,
                    newArrayList("provider was registered without any '" + communicationInterface + "' capabilities")
                )
            ));

        return new ExpectationValidationResult(
            provider.getName(),
            provider.getVersion(),
            validatedInteractions
        );
    }

    public abstract C asCapabilities(String rawCapabilities);

    public abstract E asExpectations(String rawExpectations);

    public abstract List<InteractionValidationResult> validate(E expectations, C capabilities);

    public enum InteractionValidationStatus {

        OK,
        FAILED

    }

    @Getter
    @AllArgsConstructor
    public static class CapabilitiesValidationResult {

        private String consumerName;
        private String consumerVersion;
        private List<InteractionValidationResult> interactionValidationResults;

        public boolean isEmpty() {
            return interactionValidationResults.isEmpty();
        }

    }

    @Getter
    @AllArgsConstructor
    public static class ExpectationValidationResult {

        private String providerName;
        private String providerVersion;

        private List<InteractionValidationResult> interactionValidationResults;

    }

    @Getter
    @AllArgsConstructor
    public static class InteractionValidationResult {

        private final String name;
        private final InteractionValidationStatus status;
        private final List<String> errors;

    }
}

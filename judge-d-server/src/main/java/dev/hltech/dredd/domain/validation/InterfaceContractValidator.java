package dev.hltech.dredd.domain.validation;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public abstract class InterfaceContractValidator<C, E> {

    private String communicationInterface;

    public InterfaceContractValidator(String communicationInterface) {
        this.communicationInterface = communicationInterface;
    }

    public String getCommunicationInterface() {
        return this.communicationInterface;
    }

    public List<CapabilitiesValidationResult> validateCapabilities(ServiceContracts provider, Collection<ServiceContracts> allConsumers) {
        return extractCapabilities(provider)
            .map(capabilities -> allConsumers
                .stream()
                .map(consumer -> validateCapabilities(capabilities, provider.getName(), consumer))
                .filter(validationResult -> !validationResult.getInteractionValidationResults().isEmpty())
                .collect(toList())
            ).orElse(emptyList());
    }

    private CapabilitiesValidationResult validateCapabilities(C capabilities, String providerName, ServiceContracts consumer) {
        List<InteractionValidationResult> validatedInteractions = consumer
            .getMappedExpectations(providerName, this.communicationInterface, this::asExpectations)
            .map(expectations -> validate(expectations, capabilities))
            .orElse(emptyList());

        return new CapabilitiesValidationResult(
            consumer.getName(),
            consumer.getVersion(),
            validatedInteractions
        );
    }

    public List<ExpectationValidationResult> validateExpectations(ServiceContracts consumer, Collection<ServiceContracts> allProviders) {
        return extractExpectations(consumer)
            .entrySet()
            .stream()
            .flatMap(pe -> validateExpectations(pe.getValue(), pe.getKey(), allProviders))
            .collect(toList());
    }

    private Stream<? extends ExpectationValidationResult> validateExpectations(E expectations, String providerName, Collection<ServiceContracts> allProviders) {
        List<ServiceContracts> matchedProviders = allProviders
            .stream()
            .filter(it -> it.getName().equals(providerName))
            .collect(toList());
        if (matchedProviders.isEmpty()) {
            return Stream.of(createProviderNotAvailableResult(providerName));
        } else {
            return matchedProviders
                .stream()
                .map(matchedProvider -> validateExpectations(expectations, matchedProvider));
        }
    }

    private ExpectationValidationResult validateExpectations(E expectations, ServiceContracts provider) {
        List<InteractionValidationResult> validatedInteractions = provider
            .getMappedCapabilities(this.communicationInterface, this::asCapabilities)
            .map(capabilities -> validate(expectations, capabilities))
            .orElseGet(() -> newArrayList(
                new InteractionValidationResult(
                    "any",
                    FAILED,
                    newArrayList("provider was registered without any '" + this.communicationInterface + "' capabilities")
                )
            ));

        return new ExpectationValidationResult(
            provider.getName(),
            provider.getVersion(),
            validatedInteractions
        );
    }

    private ExpectationValidationResult createProviderNotAvailableResult(String providerName) {
        return new ExpectationValidationResult(
            providerName,
            null,
            newArrayList(new InteractionValidationResult("any", InteractionValidationStatus.FAILED, newArrayList("provider not available")))
        );
    }

    public Optional<C> extractCapabilities(ServiceContracts serviceContracts) {
        return serviceContracts.getMappedCapabilities(this.communicationInterface, this::asCapabilities);
    }

    public Map<String, E> extractExpectations(ServiceContracts testedServiceContracts) {
        return testedServiceContracts.getMappedExpectations(this.communicationInterface, this::asExpectations);
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
            return this.interactionValidationResults.isEmpty();
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
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InteractionValidationResult {

        private final String name;
        private final InteractionValidationStatus status;
        private final List<String> errors;

        public static InteractionValidationResult success(String name) {
            return new InteractionValidationResult(name, OK, newArrayList());
        }

        public static InteractionValidationResult fail(String name, List<String> errors) {
            return new InteractionValidationResult(name, FAILED, errors);
        }
    }
}

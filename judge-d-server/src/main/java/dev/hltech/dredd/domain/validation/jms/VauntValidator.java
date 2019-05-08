package dev.hltech.dredd.domain.validation.jms;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VauntValidator {

    public List<ValidationResult> validate(Service consumer, Service provider) {
        return consumer.getExpectations().getProviderNameToContracts().get(provider.getName()).stream()
                .map(consumerContract -> validateWithMatchingProviderContract(
                        consumerContract, provider.getCapabilities().getContracts()))
                .collect(Collectors.toList());
    }

    public List<ValidationResult> validate(List<Contract> expectations, List<Contract> capabilities) {
        return expectations.stream()
                .map(consumerContract -> validateWithMatchingProviderContract(consumerContract, capabilities))
                .collect(Collectors.toList());
    }

    private ValidationResult validateWithMatchingProviderContract(
            Contract consumerContract, List<Contract> providerContracts) {
        List<Contract> contracts = providerContracts.stream()
                .filter(providerContract -> isEndpointMatching(consumerContract, providerContract))
                .collect(Collectors.toList());

        if (contracts.isEmpty()) {
            return ValidationResult.failure(consumerContract, providerContracts, ValidationError.MISSING_ENDPOINT);
        }

        Optional<Contract> matchingProviderContract = contracts.stream()
            .filter(providerContract -> isSchemaMatching(consumerContract, providerContract))
            .findFirst();

        if (!matchingProviderContract.isPresent()) {
            return ValidationResult.failure(consumerContract, providerContracts, ValidationError.WRONG_SCHEMA);
        }

        return ValidationResult.success(consumerContract, providerContracts);
    }

    private boolean isEndpointMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getDestinationType().equals(secondContract.getDestinationType())
                && firstContract.getDestinationName().equals(secondContract.getDestinationName());
    }

    private boolean isSchemaMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getBody().equals(secondContract.getBody());
    }

}

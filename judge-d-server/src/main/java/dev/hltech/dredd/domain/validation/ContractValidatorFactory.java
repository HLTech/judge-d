package dev.hltech.dredd.domain.validation;

public interface ContractValidatorFactory {

    ExpectationsValidator createExpectations(String expectations);

    CapabilitiesValidator createCapabilities(String providerName, String capabilities);

}

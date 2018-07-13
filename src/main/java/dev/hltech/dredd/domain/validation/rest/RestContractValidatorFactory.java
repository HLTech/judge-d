package dev.hltech.dredd.domain.validation.rest;

import dev.hltech.dredd.domain.validation.ContractValidatorFactory;
import dev.hltech.dredd.domain.validation.CapabilitiesValidator;
import dev.hltech.dredd.domain.validation.ExpectationsValidator;

public class RestContractValidatorFactory implements ContractValidatorFactory {

    @Override
    public ExpectationsValidator createExpectations(String expectations) {
        return new PactExpectationsValidator(expectations);
    }

    @Override
    public CapabilitiesValidator createCapabilities(String providerName, String capabilities) {
        return new SwaggerCapabilitiesValidator(providerName, capabilities);
    }
}

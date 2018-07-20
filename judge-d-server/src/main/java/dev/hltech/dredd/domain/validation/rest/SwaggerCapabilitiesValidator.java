package dev.hltech.dredd.domain.validation.rest;

import dev.hltech.dredd.domain.validation.CapabilitiesValidationResult;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.CapabilitiesValidator;

public class SwaggerCapabilitiesValidator implements CapabilitiesValidator {

    private final String providerName;

    public SwaggerCapabilitiesValidator(String providerName, String capabilities) {
        this.providerName = providerName;
        // parse swagger file
    }

    @Override
    public CapabilitiesValidationResult validate(ServiceContracts consumer) {
        return null;
    }
}

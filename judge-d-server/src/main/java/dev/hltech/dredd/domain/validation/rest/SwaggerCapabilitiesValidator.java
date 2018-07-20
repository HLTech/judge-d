package dev.hltech.dredd.domain.validation.rest;

import au.com.dius.pact.model.RequestResponsePact;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.CapabilitiesValidationResult;
import dev.hltech.dredd.domain.validation.CapabilitiesValidator;

import static au.com.dius.pact.model.PactReader.loadPact;

public class SwaggerCapabilitiesValidator extends RestContractValidator implements CapabilitiesValidator {

    private final String providerName;
    private String swagger;

    public SwaggerCapabilitiesValidator(String providerName, String swagger) {
        this.providerName = providerName;
        this.swagger = swagger;
    }

    @Override
    public CapabilitiesValidationResult validate(ServiceContracts consumer) {
        return new CapabilitiesValidationResult(
            validate(
                (RequestResponsePact) loadPact(consumer.getExpectations(providerName, RestContractValidatorFactory.COMMUNICATION_INTERFACE).get()),
                swagger
            )
        );
    }
}

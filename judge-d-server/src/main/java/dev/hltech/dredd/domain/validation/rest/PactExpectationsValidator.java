package dev.hltech.dredd.domain.validation.rest;

import au.com.dius.pact.model.RequestResponsePact;
import dev.hltech.dredd.domain.validation.ExpectationValidationResult;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.ExpectationsValidator;

import static au.com.dius.pact.model.PactReader.loadPact;

public class PactExpectationsValidator extends RestContractValidator implements ExpectationsValidator {

    private final RequestResponsePact requestResponsePact;

    public PactExpectationsValidator(String pact) {
        requestResponsePact = (RequestResponsePact) loadPact(pact);
    }

    @Override
    public ExpectationValidationResult validate(ServiceContracts serviceContracts) {
        return new ExpectationValidationResult(
            serviceContracts.getName(),
            serviceContracts.getVersion(),
            validate(requestResponsePact, serviceContracts.getCapabilities(RestContractValidatorFactory.COMMUNICATION_INTERFACE).get())
        );
    }
}

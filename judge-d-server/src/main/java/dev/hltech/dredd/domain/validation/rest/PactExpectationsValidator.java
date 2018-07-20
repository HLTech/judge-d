package dev.hltech.dredd.domain.validation.rest;

import dev.hltech.dredd.domain.validation.ExpectationValidationResult;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.ExpectationsValidator;

public class PactExpectationsValidator implements ExpectationsValidator {


    public PactExpectationsValidator(String expectations) {
        // parse pact file
    }

    @Override
    public ExpectationValidationResult validate(ServiceContracts serviceContracts) {
        return null;
    }
}

package dev.hltech.dredd.domain.validation;

import lombok.Getter;

import java.util.List;

import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.CapabilitiesValidationResult;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.ExpectationValidationResult;

@Getter
public class EnvironmentValidatorResult {

    private final String communicationInterface;
    private final List<CapabilitiesValidationResult> capabilitiesValidationResults;
    private final List<ExpectationValidationResult> expectationValidationResults;

    public EnvironmentValidatorResult(
        String communicationInterface,
        List<CapabilitiesValidationResult> capabilitiesValidationResults,
        List<ExpectationValidationResult> expectationValidationResults
    ) {
        this.communicationInterface = communicationInterface;
        this.capabilitiesValidationResults = capabilitiesValidationResults;
        this.expectationValidationResults = expectationValidationResults;
    }
}

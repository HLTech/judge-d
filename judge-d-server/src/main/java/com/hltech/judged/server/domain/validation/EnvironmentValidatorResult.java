package com.hltech.judged.server.domain.validation;

import lombok.Getter;

import java.util.List;

@Getter
public class EnvironmentValidatorResult {

    private final String communicationInterface;
    private final List<InterfaceContractValidator.CapabilitiesValidationResult> capabilitiesValidationResults;
    private final List<InterfaceContractValidator.ExpectationValidationResult> expectationValidationResults;

    public EnvironmentValidatorResult(
        String communicationInterface,
        List<InterfaceContractValidator.CapabilitiesValidationResult> capabilitiesValidationResults,
        List<InterfaceContractValidator.ExpectationValidationResult> expectationValidationResults
    ) {
        this.communicationInterface = communicationInterface;
        this.capabilitiesValidationResults = capabilitiesValidationResults;
        this.expectationValidationResults = expectationValidationResults;
    }
}

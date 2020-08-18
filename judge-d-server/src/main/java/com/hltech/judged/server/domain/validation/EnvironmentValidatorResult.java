package com.hltech.judged.server.domain.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EnvironmentValidatorResult {

    private final String communicationInterface;
    private final List<InterfaceContractValidator.CapabilitiesValidationResult> capabilitiesValidationResults;
    private final List<InterfaceContractValidator.ExpectationValidationResult> expectationValidationResults;
}

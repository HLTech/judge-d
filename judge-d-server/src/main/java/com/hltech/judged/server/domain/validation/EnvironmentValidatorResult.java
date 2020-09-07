package com.hltech.judged.server.domain.validation;

import com.hltech.judged.server.domain.contracts.ServiceContracts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class EnvironmentValidatorResult {

    private final String communicationInterface;
    private final List<InterfaceContractValidator.CapabilitiesValidationResult> capabilitiesValidationResults;
    private final List<InterfaceContractValidator.ExpectationValidationResult> expectationValidationResults;

    public static <C, E> EnvironmentValidatorResult getValidatorResult(
        ServiceContracts validatedService,
        Collection<ServiceContracts> environmentContracts,
        InterfaceContractValidator<C, E> validator
    ) {
        return new EnvironmentValidatorResult(
            validator.getCommunicationInterface(),
            validator.validateCapabilities(validatedService, environmentContracts),
            validator.validateExpectations(validatedService, environmentContracts)
        );
    }
}

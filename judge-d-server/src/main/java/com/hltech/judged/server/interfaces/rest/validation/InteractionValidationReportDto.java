package com.hltech.judged.server.interfaces.rest.validation;

import com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class InteractionValidationReportDto {

    private final String communicationInterface;
    private final String interactionName;
    private final InteractionValidationStatus validationResult;
    private final List<String> errors;

}

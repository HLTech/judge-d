package dev.hltech.dredd.interfaces.rest.validation;

import dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InteractionValidationReportDto {

    private String communicationInterface;
    private String interactionName;
    private InteractionValidationStatus validationResult;
    private List<String> errors;

}

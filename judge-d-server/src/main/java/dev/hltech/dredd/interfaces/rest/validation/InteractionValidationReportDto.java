package dev.hltech.dredd.interfaces.rest.validation;

import dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InteractionValidationReportDto {

    private String interactionName;
    private InteractionValidationStatus validationResult;
    private List<String> errors;

}

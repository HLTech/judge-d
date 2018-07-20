package dev.hltech.dredd.interfaces.rest;

import dev.hltech.dredd.domain.validation.InteractionValidationReport.InteractionValidationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InteractionValidationReportDto {

    private String interactionName;
    private InteractionValidationResult validationResult;
    private List<String> errors;

}

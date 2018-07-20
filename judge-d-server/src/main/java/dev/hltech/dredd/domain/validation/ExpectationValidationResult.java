package dev.hltech.dredd.domain.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExpectationValidationResult {

    private String providerName;
    private String providerVersion;

    private List<InteractionValidationReport> interactionValidationReports;

}

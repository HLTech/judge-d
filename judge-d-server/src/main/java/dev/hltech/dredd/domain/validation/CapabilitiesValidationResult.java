package dev.hltech.dredd.domain.validation;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class CapabilitiesValidationResult {

    private List<InteractionValidationReport> interactionValidationReports;

    public boolean isEmpty() {
        return interactionValidationReports.isEmpty();
    }

}

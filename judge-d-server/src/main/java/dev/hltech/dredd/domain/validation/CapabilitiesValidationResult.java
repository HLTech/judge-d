package dev.hltech.dredd.domain.validation;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class CapabilitiesValidationResult {

    public static final CapabilitiesValidationResult EMPTY = new CapabilitiesValidationResult(ImmutableList.of());

    private List<InteractionValidationReport> interactionValidationReports;

    public boolean isEmpty() {
        return interactionValidationReports.isEmpty();
    }

}

package dev.hltech.dredd.domain.validation.jms;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ValidationResult {
    private final boolean isValid;
    private final String description;
    private final List<ValidationError> errors;

    public static ValidationResult success(Contract expectation, List<Contract> capabilities) {
        return new ValidationResult(true, String.format("Expectation: %s, capabilities: %s", expectation, capabilities), new ArrayList<>());
    }

    public static ValidationResult failure(Contract expectation, List<Contract> capabilities, ValidationError... errors) {
        return new ValidationResult(false, String.format("Expectation: %s, capabilities: %s", expectation, capabilities), Arrays.asList(errors));
    }
}

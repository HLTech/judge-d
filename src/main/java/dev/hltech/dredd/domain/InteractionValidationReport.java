package dev.hltech.dredd.domain;


import java.util.List;

public class InteractionValidationReport {

    private final String name;
    private final ValidationStatus status;
    private final List<String> errors;

    public InteractionValidationReport(String name, ValidationStatus status, List<String> errors) {
        this.name = name;
        this.status = status;
        this.errors = errors;
    }

    public String getName() {
        return name;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}

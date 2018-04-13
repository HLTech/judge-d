package dev.hltech.dredd.domain;


import java.util.List;

public class InteractionValidationReport {

    private final String name;
    private final InteractionValidationStatus status;
    private final List<String> errors;

    public InteractionValidationReport(String name, InteractionValidationStatus status, List<String> errors) {
        this.name = name;
        this.status = status;
        this.errors = errors;
    }

    public String getName() {
        return name;
    }

    public InteractionValidationStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public enum InteractionValidationStatus {

        OK,
        FAILED

    }
}

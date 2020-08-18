package com.hltech.judged.server.domain.validation.jms;

import com.hltech.vaunt.core.VauntSerializer;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.validator.ValidationResult;
import com.hltech.vaunt.validator.VauntValidator;
import com.hltech.judged.server.domain.validation.InterfaceContractValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.FAILED;

@Slf4j
public class JmsContractValidator extends InterfaceContractValidator<List<Contract>, List<Contract>> {

    public static final String COMMUNICATION_INTERFACE = "jms";

    public JmsContractValidator() {
        super(COMMUNICATION_INTERFACE);
    }

    @Override
    public List<Contract> asCapabilities(String rawCapabilities) {
        return new VauntSerializer().parseContracts(rawCapabilities);
    }

    @Override
    public List<Contract> asExpectations(String rawExpectations) {
        return new VauntSerializer().parseContracts(rawExpectations);
    }

    @Override
    public List<InteractionValidationResult> validate(List<Contract> expectations, List<Contract> capabilities) {
        List<ValidationResult> validationResults = new VauntValidator().validate(expectations, capabilities);

        return validationResults.stream()
            .map(this::toInteractionValidationResult)
            .collect(Collectors.toList());
    }

    private InteractionValidationResult toInteractionValidationResult(ValidationResult validationResult) {
        if (validationResult.getResult().equals(FAILED)) {
            return InteractionValidationResult.fail(validationResult.getName(), validationResult.getErrors());
        }

        return InteractionValidationResult.success(validationResult.getName());
    }
}

package dev.hltech.dredd.domain.validation.jms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.validator.ValidationError;
import com.hltech.vaunt.validator.ValidationResult;
import com.hltech.vaunt.validator.VauntValidator;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JmsContractValidator extends InterfaceContractValidator<List<Contract>, List<Contract>> {

    public static final String COMMUNICATION_INTERFACE = "jms";

    private final ObjectMapper objectMapper;

    public JmsContractValidator() {
        super(COMMUNICATION_INTERFACE);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public List<Contract> asCapabilities(String rawCapabilities) {
        try {
            return objectMapper.readValue(rawCapabilities, new TypeReference<List<Contract>>(){});
        } catch (IOException e) {
            //TODO: after importing vaunt, provide handling
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Contract> asExpectations(String rawExpectations) {
        try {
            return objectMapper.readValue(rawExpectations, new TypeReference<List<Contract>>(){});
        } catch (IOException e) {
            //TODO: after importing vaunt, provide handling
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<InteractionValidationResult> validate(List<Contract> expectations, List<Contract> capabilities) {
        List<ValidationResult> validationResults = new VauntValidator().validate(expectations, capabilities);

        return validationResults.stream()
            .map(this::toInteractionValidationResult)
            .collect(Collectors.toList());
    }

    private InteractionValidationResult toInteractionValidationResult(ValidationResult validationResult) {
        if (!validationResult.isValid()) {
            List<String> errors = validationResult.getErrors().stream()
                .map(ValidationError::getDescription)
                .collect(Collectors.toList());
            return InteractionValidationResult.fail(validationResult.toString(), errors);
        }
        return InteractionValidationResult.success(validationResult.toString());
    }
}

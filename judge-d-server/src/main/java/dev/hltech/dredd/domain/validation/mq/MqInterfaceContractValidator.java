package dev.hltech.dredd.domain.validation.mq;

import com.google.common.collect.ImmutableList;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MqInterfaceContractValidator extends InterfaceContractValidator<String, String> {

    private static final String COMMUNICATION_INTERFACE = "mq";

    public MqInterfaceContractValidator() {
        super(COMMUNICATION_INTERFACE);
    }

    @Override
    public String asCapabilities(String rawCapabilities) {
        return rawCapabilities;
    }

    @Override
    public String asExpectations(String rawExpectations) {
        return rawExpectations;
    }

    @Override
    public List<InteractionValidationResult> validate(String expectations, String capabilities) {
        return ImmutableList.of();
    }
}

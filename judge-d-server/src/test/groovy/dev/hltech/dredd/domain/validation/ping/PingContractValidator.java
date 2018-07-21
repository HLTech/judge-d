package dev.hltech.dredd.domain.validation.ping;

import dev.hltech.dredd.domain.validation.InterfaceContractValidator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK;

public class PingContractValidator extends InterfaceContractValidator<String, String> {

    public static final String COMMUNICATION_INTERFACE = "ping";

    public PingContractValidator() {
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

    public List<InteractionValidationResult> validate(String expectations, String capabilities) {
        if (expectations.equals(capabilities)) {
            return newArrayList(new InteractionValidationResult("ping", OK, newArrayList()));
        } else {
            return newArrayList(new InteractionValidationResult("ping", FAILED, newArrayList("expected ping of " + expectations + " but found: " + capabilities)));
        }
    }


}

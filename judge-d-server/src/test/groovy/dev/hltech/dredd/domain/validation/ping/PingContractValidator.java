package dev.hltech.dredd.domain.validation.ping;

import dev.hltech.dredd.domain.validation.InterfaceContractValidator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationResult.fail;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationResult.success;

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

    @Override
    public List<InteractionValidationResult> validate(String expectations, String capabilities) {
        if (expectations.equals(capabilities)) {
            return newArrayList(success("ping"));
        } else {
            return newArrayList(fail("ping", newArrayList("expected ping of " + expectations + " but found: " + capabilities)));
        }
    }


}

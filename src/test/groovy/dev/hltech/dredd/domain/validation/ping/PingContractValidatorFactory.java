package dev.hltech.dredd.domain.validation.ping;

import dev.hltech.dredd.domain.CapabilitiesValidationResult;
import dev.hltech.dredd.domain.ExpectationValidationResult;
import dev.hltech.dredd.domain.InteractionValidationReport;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.validation.ContractValidatorFactory;
import dev.hltech.dredd.domain.validation.CapabilitiesValidator;
import dev.hltech.dredd.domain.validation.ExpectationsValidator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.FAILED;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.OK;

public class PingContractValidatorFactory implements ContractValidatorFactory {

    private static final String PROTOCOL = "ping";

    @Override
    public ExpectationsValidator createExpectations(String expectations) {
        return new ExpectationsValidator() {
            @Override
            public ExpectationValidationResult validate(ServiceContracts provider) {
                List<InteractionValidationReport> validatedInteractions = provider.getCapabilities(PROTOCOL)
                    .map(capabilities -> PingContractValidatorFactory.this.validate(expectations, capabilities))
                    .orElseGet(() -> newArrayList(
                        new InteractionValidationReport(
                            "ping",
                            FAILED,
                            newArrayList("provider was registered without any '"+PROTOCOL+"' capabilities")
                        )
                    ));

                return new ExpectationValidationResult(
                    provider.getName(),
                    provider.getVersion(),
                    validatedInteractions
                );
            }
        };
    }

    @Override
    public CapabilitiesValidator createCapabilities(String providerName, String capabilities) {
        return new CapabilitiesValidator() {
            @Override
            public CapabilitiesValidationResult validate(ServiceContracts consumer) {
                return consumer.getExpectations(providerName, "ping")
                    .map(expectations -> new CapabilitiesValidationResult(PingContractValidatorFactory.this.validate(expectations, capabilities)))
                    .orElse(CapabilitiesValidationResult.EMPTY);
            }
        };
    }

    private List<InteractionValidationReport> validate(String expectations, String capabilities) {
        if (expectations.equals(capabilities)){
            return newArrayList(new InteractionValidationReport("ping", OK, newArrayList()));
        } else {
            return newArrayList(new InteractionValidationReport("ping", FAILED, newArrayList("expected ping of "+expectations+" but found: "+capabilities)));
        }
    }

}

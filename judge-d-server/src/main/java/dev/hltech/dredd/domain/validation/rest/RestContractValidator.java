package dev.hltech.dredd.domain.validation.rest;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.pact.PactResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static au.com.dius.pact.model.PactReader.loadPact;
import static com.atlassian.oai.validator.pact.PactRequest.of;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED;
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK;
import static java.util.function.Function.identity;

@Component
public class RestContractValidator extends InterfaceContractValidator<String, RequestResponsePact> {

    public static final String COMMUNICATION_INTERFACE = "rest";

    public RestContractValidator() {
        super(COMMUNICATION_INTERFACE);
    }

    public List<InteractionValidationResult> validate(RequestResponsePact pact, String capabilities) {
        SwaggerRequestResponseValidator swaggerValidator = SwaggerRequestResponseValidator.createFor(capabilities).build();

        Map<RequestResponseInteraction, ValidationReport> validationReports = pact.getInteractions()
            .stream()
            .collect(Collectors.toMap(
                identity(),
                interaction -> swaggerValidator.validate(of(interaction.getRequest()), PactResponse.of(interaction.getResponse()))
            ));

        List<InteractionValidationResult> collect = validationReports
            .entrySet()
            .stream()
            .map(e -> {
                ValidationReport validationReport = e.getValue();
                return new InteractionValidationResult(
                    e.getKey().getDescription(),
                    validationReport.hasErrors() ? FAILED : OK,
                    validationReport.getMessages().stream().map(
                        message -> message.getMessage()
                    ).collect(Collectors.toList())
                );
            })
            .collect(Collectors.toList());

        return collect;
    }

    @Override
    public String asCapabilities(String rawCapabilities) {
        return rawCapabilities;
    }

    @Override
    public RequestResponsePact asExpectations(String rawExpectations) {
        return (RequestResponsePact) loadPact(rawExpectations);
    }

}

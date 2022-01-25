package com.hltech.judged.server.domain.validation.rest;

import au.com.dius.pact.core.model.DefaultPactReader;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.pact.PactResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.hltech.judged.server.domain.validation.InterfaceContractValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.pact.PactRequest.of;
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationResult.fail;
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationResult.success;
import static java.util.function.Function.identity;

public class RestContractValidator extends InterfaceContractValidator<String, RequestResponsePact> {

    private static final String COMMUNICATION_INTERFACE = "rest";

    public RestContractValidator() {
        super(COMMUNICATION_INTERFACE);
    }

    @Override
    public List<InteractionValidationResult> validate(RequestResponsePact pact, String capabilities) {
        OpenApiInteractionValidator swaggerValidator = OpenApiInteractionValidator.createForInlineApiSpecification(capabilities).build();

        Map<RequestResponseInteraction, ValidationReport> validationReports = pact.getInteractions()
            .stream()
            .collect(Collectors.toMap(
                identity(),
                interaction -> swaggerValidator.validate(of(interaction.getRequest()), PactResponse.of(interaction.getResponse()))
            ));

        return validationReports
            .entrySet()
            .stream()
            .map(e -> {
                ValidationReport validationReport = e.getValue();
                if (validationReport.hasErrors()) {
                    return fail(
                        e.getKey().getDescription(),
                        validationReport.getMessages().stream()
                            .map(ValidationReport.Message::getMessage)
                            .collect(Collectors.toList())
                    );
                } else {
                    return success(e.getKey().getDescription());
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public String asCapabilities(String rawCapabilities) {
        return rawCapabilities;
    }

    @Override
    public RequestResponsePact asExpectations(String rawExpectations) {
        return (RequestResponsePact) DefaultPactReader.INSTANCE.loadPact(rawExpectations);
    }

}

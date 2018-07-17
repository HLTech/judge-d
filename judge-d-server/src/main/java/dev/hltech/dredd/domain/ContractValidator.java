package dev.hltech.dredd.domain;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.pact.PactResponse;
import com.atlassian.oai.validator.report.ValidationReport;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.pact.PactRequest.of;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.FAILED;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.OK;

public class ContractValidator {

    protected List<InteractionValidationReport> validate(RequestResponsePact pact, String swagger) {
        SwaggerRequestResponseValidator swaggerValidator = SwaggerRequestResponseValidator.createFor(swagger).build();

        Map<RequestResponseInteraction, ValidationReport> validationReports = pact.getInteractions()
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                interaction -> swaggerValidator.validate(of(interaction.getRequest()), PactResponse.of(interaction.getResponse()))
            ));

        List<InteractionValidationReport> collect = validationReports
            .entrySet()
            .stream()
            .map(e -> createInteractionValidationReport(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        return collect;
    }

    protected InteractionValidationReport createInteractionValidationReport(RequestResponseInteraction key, ValidationReport validationReport) {
        return new InteractionValidationReport(
            key.getDescription(),
            validationReport.hasErrors() ? FAILED : OK,
            validationReport.getMessages().stream().map(
                message -> message.getMessage()
            ).collect(Collectors.toList())
        );
    }


}

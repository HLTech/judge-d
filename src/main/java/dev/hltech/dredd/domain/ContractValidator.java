package dev.hltech.dredd.domain;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.report.ValidationReport;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.pact.PactRequest.of;
import static com.atlassian.oai.validator.pact.PactResponse.of;

public class ContractValidator {

    private final Environment environment;

    public ContractValidator(Environment environment) {
        this.environment = environment;
    }

    public List<InteractionValidationReport> validate(RequestResponsePact pact) {
        String providerName = pact.getProvider().getName();
        String swaggerJson = environment.findServices(providerName)
            .iterator().next()
            .asProvider()
            .map(Provider::swagger)
            .orElse("");// TODO: handle this situation properly

        return validateInteractions(swaggerJson, pact.getInteractions())
            .entrySet()
            .stream()
            .map(entry -> toValidationReport(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    public Map<RequestResponseInteraction, ValidationReport> validateInteractions(String swaggerJson, List<RequestResponseInteraction> interactions) {
        SwaggerRequestResponseValidator swaggerValidator = SwaggerRequestResponseValidator.createFor(swaggerJson).build();
        return interactions.stream().collect(Collectors.toMap(
            Function.identity(),
            o -> swaggerValidator.validate(of(o.getRequest()), of(o.getResponse()))
        ));
    }

    private InteractionValidationReport toValidationReport(RequestResponseInteraction interaction, ValidationReport validationReport) {
        return new InteractionValidationReport(
            interaction.getDescription(),
            validationReport.hasErrors() ? ValidationStatus.FAILED : ValidationStatus.OK,
            validationReport.getMessages().stream().map(
                message -> message.getMessage()
            ).collect(Collectors.toList())
        );
    }

}

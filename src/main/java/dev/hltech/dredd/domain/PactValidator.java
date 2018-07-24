package dev.hltech.dredd.domain;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.report.ValidationReport;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.pact.PactRequest.of;
import static com.atlassian.oai.validator.pact.PactResponse.of;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.FAILED;
import static dev.hltech.dredd.domain.InteractionValidationReport.InteractionValidationResult.OK;

public class PactValidator {

    private final Environment environment;

    public PactValidator(Environment environment) {
        this.environment = environment;
    }

    public List<PactValidationReport> validate(RequestResponsePact pact) throws ProviderNotAvailableException {
        List<Service> providers = environment.findServices(pact.getProvider().getName())
            .stream()
            .filter(service -> service.asProvider().isPresent())
            .collect(Collectors.toList());

        if (providers.isEmpty())
            throw new ProviderNotAvailableException();


        return providers
            .stream()
            .map( service -> new PactValidationReport(
                pact.getConsumer().getName(),
                service.getName(),
                service.getVersion(),
                validate(pact, service.asProvider().get().getSwagger())
            ))
            .collect(Collectors.toList());
    }

    private List<InteractionValidationReport> validate(RequestResponsePact pact, String swagger) {
        SwaggerRequestResponseValidator swaggerValidator = SwaggerRequestResponseValidator.createFor(swagger).build();

        Map<RequestResponseInteraction, ValidationReport> validationReports = pact.getInteractions()
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                interaction -> swaggerValidator.validate(of(interaction.getRequest()), of(interaction.getResponse()))
            ));

        List<InteractionValidationReport> collect = validationReports
            .entrySet()
            .stream()
            .map(e -> createInteractionValidationReport(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        return collect;
    }

    private InteractionValidationReport createInteractionValidationReport(RequestResponseInteraction key, ValidationReport validationReport) {
        return new InteractionValidationReport(
            key.getDescription(),
            validationReport.hasErrors() ? FAILED : OK,
            validationReport.getMessages().stream().map(
                message -> message.getMessage()
            ).collect(Collectors.toList())
        );
    }

}

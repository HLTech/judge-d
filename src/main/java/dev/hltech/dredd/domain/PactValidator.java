package dev.hltech.dredd.domain;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.report.ValidationReport;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Provider;
import dev.hltech.dredd.domain.environment.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.pact.PactRequest.of;
import static com.atlassian.oai.validator.pact.PactResponse.of;

public class PactValidator {

    private final Environment environment;

    public PactValidator(Environment environment) {
        this.environment = environment;
    }

    public List<PactValidationReport> validate(RequestResponsePact pact) throws ProviderNotAvailableException {
        List<Provider> providers = environment.findServices(pact.getProvider().getName())
            .stream()
            .map(Service::asProvider)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (providers.isEmpty())
            throw new ProviderNotAvailableException();


        return providers
            .stream()
            .map( provider -> validate(pact, provider))
            .collect(Collectors.toList());
    }

    private PactValidationReport validate(RequestResponsePact pact, Provider provider) {
        SwaggerRequestResponseValidator swaggerValidator = SwaggerRequestResponseValidator.createFor(provider.getSwagger()).build();

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

        return new PactValidationReport(pact.getConsumer().getName(), provider, collect);
    }

    private InteractionValidationReport createInteractionValidationReport(RequestResponseInteraction key, ValidationReport validationReport) {
        return new InteractionValidationReport(
            key.getDescription(),
            validationReport.hasErrors() ? InteractionValidationReport.InteractionValidationStatus.FAILED : InteractionValidationReport.InteractionValidationStatus.OK,
            validationReport.getMessages().stream().map(
                message -> message.getMessage()
            ).collect(Collectors.toList())
        );
    }

}

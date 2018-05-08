package dev.hltech.dredd.domain;

import au.com.dius.pact.model.RequestResponsePact;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Service;

import java.util.List;
import java.util.stream.Collectors;

public class PactValidator extends ContractValidator {

    private final Environment environment;

    public PactValidator(Environment environment) {
        this.environment = environment;
    }

    public List<PactValidationReport> validate(RequestResponsePact pact) throws ProviderNotAvailableException {
        List<Service> providers = environment.findServices(pact.getProvider().getName())
            .stream()
            .filter(service -> service.asProvider().getSwagger().isPresent())
            .collect(Collectors.toList());

        if (providers.isEmpty())
            throw new ProviderNotAvailableException();


        return providers
            .stream()
            .map( service -> new PactValidationReport(
                pact.getConsumer().getName(),
                service.getName(),
                service.getVersion(),
                validate(pact, service.asProvider().getSwagger().get())
            ))
            .collect(Collectors.toList());
    }
}

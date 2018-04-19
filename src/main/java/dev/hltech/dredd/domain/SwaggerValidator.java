package dev.hltech.dredd.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SwaggerValidator extends ContractValidator {

    private final Environment environment;

    public SwaggerValidator(Environment environment) {
        this.environment = environment;
    }

    public List<SwaggerValidationReport> validate(String providerName, ObjectNode swagger) {
        List<Service> consumers = environment.getAllServices()
            .stream()
            .filter(service -> service.asConsumer().isPresent())
            .filter(service -> service.asConsumer().get().getPact(providerName).isPresent())
            .collect(toList());

        return consumers
            .stream()
            .map(consumer -> validate(providerName, swagger, consumer))
            .collect(toList());
    }

    private SwaggerValidationReport validate(String providerName, ObjectNode swagger, Service consumer) {
        return new SwaggerValidationReport(
            consumer.getName(),
            consumer.getVersion(),
            providerName,
            validate(consumer.asConsumer().get().getPact(providerName).get(), swagger.toString())
        );
    }

}

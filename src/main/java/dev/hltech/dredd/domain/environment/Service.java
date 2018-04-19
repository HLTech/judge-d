package dev.hltech.dredd.domain.environment;

import java.util.Optional;

public interface Service {

    String getName();

    String getVersion();

    default Optional<Provider> asProvider() {
        return Optional.empty();
    }

    default Optional<Consumer> asConsumer() {
        return Optional.empty();
    }

}

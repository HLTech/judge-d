package dev.hltech.dredd.domain.environment;

import java.util.Optional;

public interface Service {

    String name();

    Optional<Provider> asProvider();

}

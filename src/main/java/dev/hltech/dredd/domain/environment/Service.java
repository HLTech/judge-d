package dev.hltech.dredd.domain.environment;

import java.util.Optional;

public interface Service {

    String getName();

    String getVersion();

    Optional<Provider> asProvider();

}

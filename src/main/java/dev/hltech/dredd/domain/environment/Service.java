package dev.hltech.dredd.domain.environment;

import java.util.Optional;

public interface Service {

    Optional<Provider> asProvider();

}

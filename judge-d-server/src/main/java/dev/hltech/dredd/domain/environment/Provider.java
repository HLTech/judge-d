package dev.hltech.dredd.domain.environment;

import java.util.Optional;

public interface Provider {

    Optional<String> getSwagger();
}

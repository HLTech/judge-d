package com.hltech.judged.server.domain.environment;

import java.util.Optional;
import java.util.Set;

public interface EnvironmentRepository {
    Optional<Environment> find(String environmentName);

    Environment persist(Environment environment);

    Set<String> getNames();
}

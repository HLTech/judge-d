package com.hltech.judged.server.domain.environment;

import java.util.Set;

public interface EnvironmentRepository {

    EnvironmentAggregate get(String environmentName);

    EnvironmentAggregate persist(EnvironmentAggregate environment);

    Set<String> getNames();
}

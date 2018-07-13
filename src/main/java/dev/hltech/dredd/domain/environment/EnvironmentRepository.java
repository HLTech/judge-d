package dev.hltech.dredd.domain.environment;

import java.util.Set;

public interface EnvironmentRepository {

    EnvironmentAggregate get(String environmentName);

    EnvironmentAggregate persist(EnvironmentAggregate environment);

    Set<String> getNames();
}

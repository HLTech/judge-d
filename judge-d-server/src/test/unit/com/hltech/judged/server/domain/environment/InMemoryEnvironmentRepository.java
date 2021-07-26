package com.hltech.judged.server.domain.environment;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InMemoryEnvironmentRepository implements EnvironmentRepository {
    private final Map<String, Environment> storage = Maps.newHashMap();

    @Override
    public Optional<Environment> find(String environmentName) {
        return Optional.ofNullable(storage.get(environmentName));
    }

    @Override
    public Environment persist(Environment environment) {
        storage.put(environment.getName(), environment);
        return environment;
    }

    @Override
    public Set<String> getNames() {
        return storage.keySet();
    }
}

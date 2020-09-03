package com.hltech.judged.server.domain.environment;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.hltech.judged.server.domain.environment.Environment.empty;

public class InMemoryEnvironmentRepository implements EnvironmentRepository {

    private Map<String, Environment> storage = Maps.newHashMap();

    @Override
    public Environment get(String environmentName) {
        return Optional.ofNullable(storage.get(environmentName))
            .orElseGet(() -> empty(environmentName));
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

package com.hltech.judged.server.domain.environment;

import java.util.Set;

public interface EnvironmentRepository {

    Environment get(String environmentName);

    Environment persist(Environment environment);

    Set<String> getNames();
}

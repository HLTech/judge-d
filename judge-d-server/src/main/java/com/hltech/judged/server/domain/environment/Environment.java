package com.hltech.judged.server.domain.environment;

import com.google.common.collect.SetMultimap;
import com.hltech.judged.server.domain.ServiceVersion;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.HashMultimap.create;

public class Environment {

    public static final String DEFAULT_NAMESPACE = "default";

    private final String name;
    private final SetMultimap<String, ServiceVersion> serviceVersions;

    public Environment(String name, SetMultimap<String, ServiceVersion> serviceVersions) {
        this.name = name;
        this.serviceVersions = serviceVersions;
    }

    public Environment(String name, Set<ServiceVersion> serviceVersions) {
        this.name = name;
        this.serviceVersions = create();
        this.serviceVersions.putAll(DEFAULT_NAMESPACE, serviceVersions);
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getSpaceNames() {
        return serviceVersions.keySet();
    }

    public Set<ServiceVersion> getServices(String space) {
       return serviceVersions.get(space);
    }

    public Set<ServiceVersion> getAllServices() {
        return serviceVersions.values().stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    public static Environment empty(String environmentName) {
        return new Environment(environmentName, new HashSet<>());
    }

    public static EnvironmentBuilder builder(String name) {
        return new EnvironmentBuilder(name);
    }

    public static class EnvironmentBuilder {

        private final String name;
        // <space, serviceVersion>
        private final SetMultimap<String, ServiceVersion> serviceVersions = create();

        private EnvironmentBuilder(String name) {
            this.name = name;
        }

        public EnvironmentBuilder withServiceVersion(String space, ServiceVersion serviceVersion) {
            this.serviceVersions.put(space, serviceVersion);
            return this;
        }

        public EnvironmentBuilder withServiceVersions(String space, Set<ServiceVersion> serviceVersions) {
            this.serviceVersions.putAll(space, serviceVersions);
            return this;
        }

        public Environment build() {
            return new Environment(this.name, this.serviceVersions);
        }

    }
}

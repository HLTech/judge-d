package com.hltech.judged.server.domain.environment;

import com.google.common.collect.Multimap;
import com.hltech.judged.server.domain.ServiceVersion;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.HashMultimap.create;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Entity
@Table(name = "environments")
@Access(AccessType.FIELD)
public class EnvironmentAggregate {

    public static final String DEFAULT_NAMESPACE = "default";

    @Id
    private String name;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = SpaceServiceVersion.class )
    @JoinTable(name = "service_versions", joinColumns = {
        @JoinColumn(name = "environment_name", referencedColumnName = "name"),
    })
    private Set<SpaceServiceVersion> serviceVersions = newHashSet();

    protected EnvironmentAggregate() {
    }

    protected EnvironmentAggregate(String name, Set< ServiceVersion> deatulSpaceServiceVersions) {
        this.name = name;
        this.serviceVersions.addAll(
            deatulSpaceServiceVersions
                .stream()
                .map(sv -> new SpaceServiceVersion(DEFAULT_NAMESPACE, sv.getName(), sv.getVersion()))
                .collect(toList())
        );
    }

    private EnvironmentAggregate(String name, Multimap<String, ServiceVersion> serviceVersions) {
        this.name = name;
        this.serviceVersions.addAll(
            serviceVersions.entries()
                .stream()
                .map(e -> new SpaceServiceVersion(e.getKey(), e.getValue().getName(), e.getValue().getVersion()))
                .collect(toList())
        );
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getSpaceNames() {
        return serviceVersions.stream().map(SpaceServiceVersion::getSpace).collect(toSet());
    }

    public Set<ServiceVersion> getServices(String space) {
       return serviceVersions
           .stream()
           .filter(ssv -> ssv.getSpace().equals(space))
           .map(ssv -> new ServiceVersion(ssv.getName(), ssv.getVersion()))
           .collect(toSet());
    }

    public Set<ServiceVersion> getAllServices() {
        return serviceVersions.stream()
            .map(ssv -> new ServiceVersion(ssv.getName(), ssv.getVersion()))
            .collect(toSet());
    }

    public static EnvironmentAggregate empty(String environmentName) {
        return new EnvironmentAggregate(environmentName, create());
    }

    public static EnvironmentAggregateBuilder builder(String name) {
        return new EnvironmentAggregateBuilder(name);
    }

    public static class EnvironmentAggregateBuilder {

        private String name;
        private Multimap<String, ServiceVersion> serviceVersions = create();

        private EnvironmentAggregateBuilder(String name) {
            this.name = name;
        }

        public EnvironmentAggregateBuilder withServiceVersion(String name, String version) {
            this.serviceVersions.put(DEFAULT_NAMESPACE, new ServiceVersion(name, version));
            return this;
        }

        public EnvironmentAggregateBuilder withServiceVersions(String space, Set<ServiceVersion> serviceVersions) {
            this.serviceVersions.putAll(space, serviceVersions);
            return this;
        }

        public EnvironmentAggregate build() {
            return new EnvironmentAggregate(
                this.name,
                this.serviceVersions
            );
        }

    }
}

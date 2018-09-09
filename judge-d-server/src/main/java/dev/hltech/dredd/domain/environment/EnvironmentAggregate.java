package dev.hltech.dredd.domain.environment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "environments")
@Access(AccessType.FIELD)
public class EnvironmentAggregate {

    @Id
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "service_versions", joinColumns = {
        @JoinColumn(name = "environment_name", referencedColumnName = "name"),
    })
    private Set<ServiceVersion> serviceVersions;

    protected EnvironmentAggregate() {
    }

    private EnvironmentAggregate(String name, Set<ServiceVersion> serviceVersions) {
        this.name = name;
        this.serviceVersions = serviceVersions;
    }

    public String getName() {
        return this.name;
    }

    public Set<ServiceVersion> getAllServices() {
        return this.serviceVersions;
    }

    public static EnvironmentAggregate empty(String environmentName) {
        return new EnvironmentAggregate(environmentName, newHashSet());
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED)
    @Access(AccessType.FIELD)
    @ToString
    public static class ServiceVersion {

        private String name;
        private String version;

    }

    public static EnvironmentAggregateBuilder builder(String name) {
        return new EnvironmentAggregateBuilder(name);
    }

    public static class EnvironmentAggregateBuilder {

        private String name;
        private Multimap<String, String> serviceVersions = HashMultimap.create();

        private EnvironmentAggregateBuilder(String name) {
            this.name = name;
        }

        public void withServiceVersion(String name, String version) {
            this.serviceVersions.put(name, version);
        }

        public EnvironmentAggregate build() {
            return new EnvironmentAggregate(
                this.name,
                this.serviceVersions.entries()
                    .stream()
                    .map(e -> new ServiceVersion(e.getKey(), e.getValue()))
                    .collect(toSet())
            );
        }

    }
}

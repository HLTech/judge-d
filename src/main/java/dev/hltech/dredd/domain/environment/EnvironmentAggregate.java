package dev.hltech.dredd.domain.environment;

import lombok.*;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name="environments")
@Access(AccessType.FIELD)
public class EnvironmentAggregate {

    @Id
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name="service_versions", joinColumns={
        @JoinColumn(name = "environment_name", referencedColumnName = "name"),
    })
    private Set<ServiceVersion> serviceVersions;

    protected EnvironmentAggregate() {
    }

    public EnvironmentAggregate(String name, Set<ServiceVersion>  serviceVersions) {
        this.name = name;
        this.serviceVersions = serviceVersions;
    }

    public String getName(){
        return name;
    }

    public Set<ServiceVersion> findServices(String serviceName) {
        return serviceVersions.stream()
            .filter(sv -> sv.getName().equals(serviceName))
            .collect(Collectors.toSet());
    }

    public Set<ServiceVersion> getAllServices() {
        return serviceVersions;
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
    public static class ServiceVersion {

        private String name;
        private String version;

    }
}

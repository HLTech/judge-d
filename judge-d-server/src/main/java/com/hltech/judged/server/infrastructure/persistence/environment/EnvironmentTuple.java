package com.hltech.judged.server.infrastructure.persistence.environment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "environments")
@Access(AccessType.FIELD)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentTuple {

    public static final String DEFAULT_NAMESPACE = "default";

    @Id
    private String name;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = ServiceVersion.class)
    @JoinTable(name = "service_versions", joinColumns = {
        @JoinColumn(name = "environment_name", referencedColumnName = "name"),
    })
    private Set<ServiceVersion> serviceVersions;
}

package com.hltech.judged.server.infrastructure.persistence.environment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
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

package com.hltech.judged.server.infrastructure.persistence.contracts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "SERVICE_CONTRACTS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceContractsTuple {

    @Delegate
    @EmbeddedId
    private ServiceVersion id;

    @MapKeyColumn(name = "protocol")
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "capabilities", joinColumns = {
        @JoinColumn(name = "service_name", referencedColumnName = "name"),
        @JoinColumn(name = "service_version", referencedColumnName = "version")
    })
    private Map<String, ContractTuple> capabilitiesPerProtocol;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "expectations", joinColumns = {
        @JoinColumn(name = "service_name", referencedColumnName = "name"),
        @JoinColumn(name = "service_version", referencedColumnName = "version")
    })
    private Map<ProviderProtocolTuple, ContractTuple> expectations;

    private Instant publicationTime;

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @Access(AccessType.FIELD)
    static class ProviderProtocolTuple {
        private String provider;
        private String protocol;
    }

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @Access(AccessType.FIELD)
    static class ContractTuple implements Serializable {
        private String value;
        private String mimeType;
    }
}

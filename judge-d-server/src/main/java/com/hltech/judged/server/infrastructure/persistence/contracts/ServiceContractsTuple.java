package com.hltech.judged.server.infrastructure.persistence.contracts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "SERVICE_CONTRACTS")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceContractsTuple {

    @EmbeddedId
    private ServiceVersion id;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "protocol")
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

    public String getName() {
        return this.id.getName();
    }

    public String getVersion() {
        return this.id.getVersion();
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    @EqualsAndHashCode
    public static class ProviderProtocolTuple {
        private String provider;
        private String protocol;
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    @EqualsAndHashCode
    public static class ContractTuple implements Serializable {
        private String value;
        private String mimeType;
    }

}

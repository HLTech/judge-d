package dev.hltech.dredd.domain.contracts;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Entity
@Access(AccessType.FIELD)
public class ServiceContracts {

    @EmbeddedId
    private ServiceContractsId id;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "protocol")
    @JoinTable(name = "capabilities", joinColumns = {
        @JoinColumn(name = "service_name", referencedColumnName = "name"),
        @JoinColumn(name = "service_version", referencedColumnName = "version")
    })
    private Map<String, Contract> capabilitiesPerProtocol;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "expectations", joinColumns = {
        @JoinColumn(name = "service_name", referencedColumnName = "name"),
        @JoinColumn(name = "service_version", referencedColumnName = "version")
    })
    private Map<ProviderProtocol, Contract> expectations;

    protected ServiceContracts() {
    }

    public ServiceContracts(String name, String version, Map<String, String> capabilitiesPerProtocol, Map<String, Map<String, String>> expectationsPerProvider) {
        this.id = new ServiceContractsId(name, version);
        this.capabilitiesPerProtocol = capabilitiesPerProtocol.entrySet().stream().collect(toMap(
            entry -> entry.getKey(),
            entry -> new Contract(entry.getValue())
        ));
        this.expectations = newHashMap();
        for (Entry<String, Map<String, String>> expectationsPerProviderEntry : expectationsPerProvider.entrySet()) {
            String provider = expectationsPerProviderEntry.getKey();
            Map<String, String> expectationsPerProtocol = expectationsPerProviderEntry.getValue();

            for (Entry<String, String> expectationsPerProtocolEntry : expectationsPerProtocol.entrySet()) {
                String protocol = expectationsPerProtocolEntry.getKey();
                String protocolExpectation = expectationsPerProtocolEntry.getValue();
                this.expectations.put(new ProviderProtocol(provider, protocol), new Contract(protocolExpectation));
            }
        }
    }

    public String getName() {
        return id.getName();
    }

    public String getVersion() {
        return id.getVersion();
    }

    public Optional<String> getCapabilities(String protocol) {
        return ofNullable(capabilitiesPerProtocol.get(protocol)).map(Contract::getValue);
    }

    public Optional<String> getExpectations(String providerName, String protocol) {
        return ofNullable(expectations.get(new ProviderProtocol(providerName, protocol))).map(Contract::getValue);
    }

    public Map<String, String> getCapabilities() {
        return capabilitiesPerProtocol.entrySet().stream().collect(toMap(
            e -> e.getKey(),
            e -> e.getValue().getValue()
        ));
    }

    public Map<String, Map<String, String>> getExpectations() {
        HashMap<String, Map<String, String>> result = newHashMap();
        for (Entry<ProviderProtocol, Contract> e : this.expectations.entrySet()) {
            ProviderProtocol pp = e.getKey();
            Contract contract = e.getValue();
            if (!result.containsKey(pp.provider)) {
                result.put(pp.provider, newHashMap());
            }
            result.get(pp.getProvider()).put(pp.getProtocol(), contract.getValue());
        }
        return result;
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    static class ServiceContractsId implements Serializable {
        private String name;
        private String version;
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    @EqualsAndHashCode
    public static class ProviderProtocol {
        private String provider;
        private String protocol;
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    public static class Contract implements Serializable {

        private String value;

    }
}

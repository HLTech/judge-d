package dev.hltech.dredd.domain.contracts;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

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
            entry -> new Contract(entry.getValue(), MediaType.APPLICATION_JSON_UTF8_VALUE)
        ));
        this.expectations = newHashMap();
        for (Entry<String, Map<String, String>> expectationsPerProviderEntry : expectationsPerProvider.entrySet()) {
            String provider = expectationsPerProviderEntry.getKey();
            Map<String, String> expectationsPerProtocol = expectationsPerProviderEntry.getValue();

            for (Entry<String, String> expectationsPerProtocolEntry : expectationsPerProtocol.entrySet()) {
                String protocol = expectationsPerProtocolEntry.getKey();
                String protocolExpectation = expectationsPerProtocolEntry.getValue();
                this.expectations.put(new ProviderProtocol(provider, protocol), new Contract(protocolExpectation, MediaType.APPLICATION_JSON_UTF8_VALUE));
            }
        }
    }

    public String getName() {
        return this.id.getName();
    }

    public String getVersion() {
        return this.id.getVersion();
    }


    public <C> Optional<C> getCapabilities(String communicationInterface, Function<String, C> deserializer) {
        return ofNullable(this.capabilitiesPerProtocol.get(communicationInterface))
            .map(Contract::getValue)
            .map(deserializer);
    }

    public <E> Optional<E> getExpectations(String providerName, String communicationInterface, Function<String, E> deserializer) {
        return ofNullable(this.expectations.get(new ProviderProtocol(providerName, communicationInterface)))
            .map(Contract::getValue)
            .map(deserializer);
    }

    public <E> Map<String, E> getExpectations(String communicationInterface, Function<String, E> deserializer) {
        return this.expectations.entrySet()
            .stream()
            .filter(expectationsEntry -> expectationsEntry.getKey().getProtocol().equals(communicationInterface))
            .collect(toMap(
                e -> e.getKey().getProvider(),
                e -> deserializer.apply(e.getValue().getValue())
            ));
    }

    public Map<String, String> getCapabilities() {
        return this.capabilitiesPerProtocol.entrySet().stream().collect(toMap(
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
        private String mimeType;
    }
}

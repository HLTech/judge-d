package com.hltech.judged.server.domain.contracts;

import com.hltech.judged.server.domain.ServiceVersion;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Getter
@Access(AccessType.FIELD)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServiceContracts {

    private final ServiceVersion id;
    private final Map<String, Contract> capabilitiesPerProtocol;
    private final Map<ProviderProtocol, Contract> expectations;

    public ServiceContracts(String name, String version, Map<String, Contract> capabilitiesPerProtocol, Map<String, Map<String, Contract>> expectationsPerProvider) {
        this.id = new ServiceVersion(name, version);
        this.capabilitiesPerProtocol = capabilitiesPerProtocol;
        this.expectations = newHashMap();
        for (Entry<String, Map<String, Contract>> expectationsPerProviderEntry : expectationsPerProvider.entrySet()) {
            String provider = expectationsPerProviderEntry.getKey();
            Map<String, Contract> expectationsPerProtocol = expectationsPerProviderEntry.getValue();

            for (Entry<String, Contract> expectationsPerProtocolEntry : expectationsPerProtocol.entrySet()) {
                String protocol = expectationsPerProtocolEntry.getKey();
                this.expectations.put(new ProviderProtocol(provider, protocol), expectationsPerProtocolEntry.getValue());
            }
        }
    }

    public String getName() {
        return this.id.getName();
    }

    public String getVersion() {
        return this.id.getVersion();
    }


    public <C> Optional<C> getMappedCapabilities(String communicationInterface, Function<String, C> deserializer) {
        return ofNullable(this.capabilitiesPerProtocol.get(communicationInterface))
            .map(Contract::getValue)
            .map(deserializer);
    }

    public <E> Optional<E> getMappedExpectations(String providerName, String communicationInterface, Function<String, E> deserializer) {
        return ofNullable(this.expectations.get(new ProviderProtocol(providerName, communicationInterface)))
            .map(Contract::getValue)
            .map(deserializer);
    }

    public <E> Map<String, E> getMappedExpectations(String communicationInterface, Function<String, E> deserializer) {
        return this.expectations.entrySet()
            .stream()
            .filter(expectationsEntry -> expectationsEntry.getKey().getProtocol().equals(communicationInterface))
            .collect(toMap(
                e -> e.getKey().getProvider(),
                e -> deserializer.apply(e.getValue().getValue())
            ));
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    @EqualsAndHashCode
    public static class ProviderProtocol {
        private String provider;
        private String protocol;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Access(AccessType.FIELD)
    @EqualsAndHashCode
    public static class Contract implements Serializable {
        private String value;
        private String mimeType;
    }
}

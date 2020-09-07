package com.hltech.judged.server.domain.contracts;

import com.hltech.judged.server.domain.ServiceId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServiceContracts {

    private final ServiceId id;
    private final List<Capability> capabilities;
    private final List<Expectation> expectations;

    public String getName() {
        return this.id.getName();
    }

    public String getVersion() {
        return this.id.getVersion();
    }

    public <C> Optional<C> getMappedCapabilities(String communicationInterface, Function<String, C> deserializer) {
        return this.capabilities.stream()
            .filter(capability -> capability.getProtocol().equals(communicationInterface))
            .findAny()
            .map(capability -> capability.getContract().getValue())
            .map(deserializer);
    }

    public <E> Optional<E> getMappedExpectations(String providerName, String communicationInterface, Function<String, E> deserializer) {
        return this.expectations.stream()
            .filter(expectation -> expectation.getProvider().equals(providerName))
            .filter(expectation -> expectation.getProtocol().equals(communicationInterface))
            .findAny()
            .map(expectation -> expectation.getContract().getValue())
            .map(deserializer);
    }

    public <E> Map<String, E> getMappedExpectations(String communicationInterface, Function<String, E> deserializer) {
        return this.expectations.stream()
            .filter(expectation -> expectation.getProtocol().equals(communicationInterface))
            .collect(toMap(
                Expectation::getProvider,
                expectation -> deserializer.apply(expectation.getContract().getValue())
            ));
    }
}

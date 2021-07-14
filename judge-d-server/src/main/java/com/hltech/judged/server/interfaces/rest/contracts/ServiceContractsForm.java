package com.hltech.judged.server.interfaces.rest.contracts;

import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.domain.contracts.Capability;
import com.hltech.judged.server.domain.contracts.Contract;
import com.hltech.judged.server.domain.contracts.Expectation;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ServiceContractsForm {

    //<protocol, contract>
    private final Map<String, ContractForm> capabilities;
    //<provider, protocol, contract>
    private final Map<String, Map<String, ContractForm>> expectations;

    ServiceContracts toDomain(String serviceName, String version) {
        return new ServiceContracts(
            new ServiceId(serviceName, version),
            mapCapabilitiesForm(this.capabilities),
            mapExpectationsForm(this.expectations)
        );
    }

    private List<Capability> mapCapabilitiesForm(Map<String, ServiceContractsForm.ContractForm> capabilities) {
        return capabilities.keySet().stream()
            .map(protocol -> new Capability(protocol, new Contract(capabilities.get(protocol).getValue(), capabilities.get(protocol).getMimeType())))
            .collect(Collectors.toList());
    }

    private List<Expectation> mapExpectationsForm(Map<String, Map<String, ServiceContractsForm.ContractForm>> expectations) {
        return expectations.keySet().stream()
            .flatMap(provider -> expectations.get(provider).keySet().stream()
                .map(protocol -> new Expectation(
                    provider, protocol,
                    new Contract(
                        expectations.get(provider).get(protocol).getValue(),
                        expectations.get(provider).get(protocol).getMimeType()
                    )
                ))
            )
            .collect(Collectors.toList());
    }

    @Data
    public static class ContractForm implements Serializable {
        private final String value;
        private final String mimeType;
    }
}

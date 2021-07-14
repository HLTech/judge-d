package com.hltech.judged.server.interfaces.rest.contracts;

import com.hltech.judged.server.domain.contracts.Capability;
import com.hltech.judged.server.domain.contracts.Expectation;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;

@Data
public class ServiceContractsDto {

    private final String name;
    private final String version;

    private final Map<String, ContractDto> capabilities;
    private final Map<String, Map<String, ContractDto>> expectations;

    public static ServiceContractsDto fromDomain(ServiceContracts serviceContracts) {
        return new ServiceContractsDto(
            serviceContracts.getName(),
            serviceContracts.getVersion(),
            mapCapabilitiesToDto(serviceContracts.getCapabilities()),
            mapExpectationsToDto(serviceContracts.getExpectations())
        );
    }

    private static Map<String, ServiceContractsDto.ContractDto> mapCapabilitiesToDto(List<Capability> capabilities) {
        return capabilities.stream()
            .collect(Collectors.toMap(
                Capability::getProtocol,
                capability -> new ServiceContractsDto.ContractDto(
                    capability.getContract().getValue(),
                    capability.getContract().getMimeType())
            ));
    }

    private static Map<String, Map<String, ServiceContractsDto.ContractDto>> mapExpectationsToDto(List<Expectation> expectations) {
        HashMap<String, Map<String, ContractDto>> result = newHashMap();
        for (Expectation expectation : expectations) {
            if (!result.containsKey(expectation.getProvider())) {
                result.put(expectation.getProvider(), newHashMap());
            }
            result.get(expectation.getProvider()).put(
                expectation.getProtocol(),
                new ServiceContractsDto.ContractDto(
                    expectation.getContract().getValue(),
                    expectation.getContract().getMimeType()
                )
            );
        }
        return result;
    }

    @Data
    private static class ContractDto implements Serializable {
        private final String value;
        private final String mimeType;
    }
}

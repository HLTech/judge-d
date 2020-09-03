package com.hltech.judged.server.interfaces.rest.contracts;

import com.hltech.judged.server.domain.contracts.Capability;
import com.hltech.judged.server.domain.contracts.Contract;
import com.hltech.judged.server.domain.contracts.Expectation;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;

@Service
public class ContractsMapper {

    List<Capability> mapCapabilitiesForm(Map<String, ServiceContractsForm.ContractForm> capabilities) {
        return capabilities.keySet().stream()
            .map(protocol -> new Capability(protocol, new Contract(capabilities.get(protocol).getValue(), capabilities.get(protocol).getMimeType())))
            .collect(Collectors.toList());
    }

    List<Expectation> mapExpectationsForm(Map<String, Map<String, ServiceContractsForm.ContractForm>> expectations) {
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

    public ServiceContractsDto toDto(ServiceContracts serviceContracts) {
        return new ServiceContractsDto(
            serviceContracts.getName(),
            serviceContracts.getVersion(),
            mapCapabilitiesToDto(serviceContracts.getCapabilities()),
            mapExpectationsToDto(serviceContracts.getExpectations())
        );
    }

    private Map<String, Map<String, ServiceContractsDto.ContractDto>> mapExpectationsToDto(List<Expectation> expectations) {
        HashMap<String, Map<String, ServiceContractsDto.ContractDto>> result = newHashMap();
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

    private Map<String, ServiceContractsDto.ContractDto> mapCapabilitiesToDto(List<Capability> capabilities) {
        return capabilities.stream()
            .collect(Collectors.toMap(
                Capability::getProtocol,
                capability -> new ServiceContractsDto.ContractDto(
                    capability.getContract().getValue(),
                    capability.getContract().getMimeType())
            ));
    }
}

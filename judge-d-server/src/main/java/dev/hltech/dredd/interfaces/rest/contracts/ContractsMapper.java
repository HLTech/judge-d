package dev.hltech.dredd.interfaces.rest.contracts;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;

@Service
public class ContractsMapper {

    Map<String, Map<String, ServiceContracts.Contract>> mapExpectationsForm(Map<String, Map<String, ServiceContractsForm.ContractForm>> expectations) {
        return expectations.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> mapCapabilitiesForm(entry.getValue())
                )
            );
    }

    Map<String, ServiceContracts.Contract> mapCapabilitiesForm(Map<String, ServiceContractsForm.ContractForm> capabilities) {
        return mapContractsForm(capabilities);
    }

    private Map<String, ServiceContracts.Contract> mapContractsForm(Map<String, ServiceContractsForm.ContractForm> protocolToContractForms) {
        return protocolToContractForms.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ServiceContracts.Contract(entry.getValue().getValue(), entry.getValue().getMimeType())
            ));
    }

    public ServiceContractsDto toDto(ServiceContracts serviceContracts) {
        return new ServiceContractsDto(
            serviceContracts.getName(),
            serviceContracts.getVersion(),
            mapCapabilitiesToDto(serviceContracts.getCapabilitiesPerProtocol()),
            mapExpectationsToDto(serviceContracts.getExpectations())
        );
    }

    private Map<String, Map<String, ServiceContractsDto.ContractDto>> mapExpectationsToDto(Map<ServiceContracts.ProviderProtocol, ServiceContracts.Contract> expectations) {
        HashMap<String, Map<String, ServiceContractsDto.ContractDto>> result = newHashMap();
        for (Map.Entry<ServiceContracts.ProviderProtocol, ServiceContracts.Contract> e : expectations.entrySet()) {
            ServiceContracts.ProviderProtocol pp = e.getKey();
            ServiceContracts.Contract contract = e.getValue();
            if (!result.containsKey(pp.getProvider())) {
                result.put(pp.getProvider(), newHashMap());
            }
            result.get(pp.getProvider()).put(pp.getProtocol(), new ServiceContractsDto.ContractDto(contract.getValue(), contract.getMimeType()));
        }
        return result;
    }

    private Map<String, ServiceContractsDto.ContractDto> mapCapabilitiesToDto(Map<String, ServiceContracts.Contract> capabilities) {
        return capabilities.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ServiceContractsDto.ContractDto(entry.getValue().getValue(), entry.getValue().getMimeType())
            ));
    }
}

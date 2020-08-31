package com.hltech.judged.server.infrastructure.persistence.contracts;

import com.hltech.judged.server.domain.ServiceVersion;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServiceContractsRepositoryImpl implements ServiceContractsRepository {

    private final ServiceContractsTupleRepository serviceContractsTupleRepository;

    @Override
    public ServiceContracts persist(ServiceContracts serviceContracts) {
        return toServiceContracts(serviceContractsTupleRepository.save(toServiceContractsTuple(serviceContracts)));
    }

    @Override
    public Optional<ServiceContracts> findOne(ServiceVersion serviceVersion) {
        return serviceContractsTupleRepository
            .findById_NameAndId_Version(serviceVersion.getName(), serviceVersion.getVersion())
            .map(this::toServiceContracts);
    }

    @Override
    public String getService(String name) {
        return serviceContractsTupleRepository.findById_Name(name).stream()
            .map(ServiceContractsTuple::getName)
            .findFirst()
            .orElseThrow(() -> new NoResultException("No service for specified name"));
    }

    @Override
    public List<ServiceContracts> findAllByServiceName(String name) {
        return serviceContractsTupleRepository.findById_Name(name).stream()
            .map(this::toServiceContracts)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getServiceNames() {
        return serviceContractsTupleRepository.findId_NameDistinct();
    }

    private ServiceContractsTuple toServiceContractsTuple(ServiceContracts serviceContracts) {
        return new ServiceContractsTuple(serviceContracts.getId(),
            serviceContracts.getCapabilitiesPerProtocol().keySet().stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    key -> new ServiceContractsTuple.ContractTuple(
                        serviceContracts.getCapabilitiesPerProtocol().get(key).getValue(),
                        serviceContracts.getCapabilitiesPerProtocol().get(key).getMimeType()
                    )
                )),
            serviceContracts.getExpectations().keySet().stream()
                .collect(Collectors.toMap(
                    key -> new ServiceContractsTuple.ProviderProtocolTuple(
                        key.getProvider(),
                        key.getProtocol()
                    ),
                    key -> new ServiceContractsTuple.ContractTuple(
                        serviceContracts.getExpectations().get(key).getValue(),
                        serviceContracts.getExpectations().get(key).getMimeType()
                    )
                ))
        );
    }

    private ServiceContracts toServiceContracts(ServiceContractsTuple serviceContractsTuple) {
        return new ServiceContracts(serviceContractsTuple.getId(),
            serviceContractsTuple.getCapabilitiesPerProtocol().keySet().stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    key -> new ServiceContracts.Contract(
                        serviceContractsTuple.getCapabilitiesPerProtocol().get(key).getValue(),
                        serviceContractsTuple.getCapabilitiesPerProtocol().get(key).getMimeType()
                    )
                )),
            serviceContractsTuple.getExpectations().keySet().stream()
                .collect(Collectors.toMap(
                    key -> new ServiceContracts.ProviderProtocol(
                        key.getProvider(),
                        key.getProtocol()
                    ),
                    key -> new ServiceContracts.Contract(
                        serviceContractsTuple.getExpectations().get(key).getValue(),
                        serviceContractsTuple.getExpectations().get(key).getMimeType()
                    )
                ))
        );
    }
}

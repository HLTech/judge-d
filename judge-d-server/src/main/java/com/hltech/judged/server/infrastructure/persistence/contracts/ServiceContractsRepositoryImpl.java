package com.hltech.judged.server.infrastructure.persistence.contracts;

import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.domain.contracts.Capability;
import com.hltech.judged.server.domain.contracts.Contract;
import com.hltech.judged.server.domain.contracts.Expectation;
import com.hltech.judged.server.domain.contracts.ServiceContracts;
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Map;
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
    public Optional<ServiceContracts> findOne(ServiceId serviceVersion) {
        return serviceContractsTupleRepository
            .findById_NameAndId_Version(serviceVersion.getName(), serviceVersion.getVersion())
            .map(this::toServiceContracts);
    }

    @Override
    public Optional<Contract> findCapabilityByServiceIdProtocol(ServiceId serviceId, String protocol) {
        return serviceContractsTupleRepository
            .findById_NameAndId_Version(serviceId.getName(), serviceId.getVersion())
            .map(sc -> sc.getCapabilitiesPerProtocol().get(protocol))
            .map(tuple -> new Contract(tuple.getValue(), tuple.getMimeType()));
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
        return new ServiceContractsTuple(
            new ServiceVersion(serviceContracts.getId().getName(), serviceContracts.getId().getVersion()),
            toCapabilities(serviceContracts),
            toExpectations(serviceContracts)
        );
    }

    private Map<ServiceContractsTuple.ProviderProtocolTuple, ServiceContractsTuple.ContractTuple>
    toExpectations(ServiceContracts serviceContracts) {
        return serviceContracts.getExpectations().stream()
            .collect(Collectors.toMap(
                expectation -> new ServiceContractsTuple.ProviderProtocolTuple(
                    expectation.getProvider(),
                    expectation.getProtocol()
                ),
                expectation -> new ServiceContractsTuple.ContractTuple(
                    expectation.getContract().getValue(),
                    expectation.getContract().getMimeType()
                )
            ));
    }

    private Map<String, ServiceContractsTuple.ContractTuple> toCapabilities(ServiceContracts serviceContracts) {
        return serviceContracts.getCapabilities().stream()
            .collect(Collectors.toMap(
                Capability::getProtocol,
                capability -> new ServiceContractsTuple.ContractTuple(
                    capability.getContract().getValue(),
                    capability.getContract().getMimeType()
                )
            ));
    }

    private ServiceContracts toServiceContracts(ServiceContractsTuple serviceContractsTuple) {
        return new ServiceContracts(
            new ServiceId(serviceContractsTuple.getId().getName(), serviceContractsTuple.getId().getVersion()),
            toCapabilities(serviceContractsTuple),
            toExpectations(serviceContractsTuple)
        );
    }

    private List<Expectation> toExpectations(ServiceContractsTuple serviceContractsTuple) {
        return serviceContractsTuple.getExpectations().keySet().stream()
            .map(providerProtocol -> new Expectation(
                providerProtocol.getProvider(),
                providerProtocol.getProtocol(),
                new Contract(
                    serviceContractsTuple.getExpectations().get(providerProtocol).getValue(),
                    serviceContractsTuple.getExpectations().get(providerProtocol).getMimeType())))
            .collect(Collectors.toList());
    }

    private List<Capability> toCapabilities(ServiceContractsTuple serviceContractsTuple) {
        return serviceContractsTuple.getCapabilitiesPerProtocol().keySet().stream()
            .map(protocol -> new Capability(
                protocol,
                new Contract(
                    serviceContractsTuple.getCapabilitiesPerProtocol().get(protocol).getValue(),
                    serviceContractsTuple.getCapabilitiesPerProtocol().get(protocol).getMimeType())))
            .collect(Collectors.toList());
    }
}

package dev.hltech.dredd.domain.contracts;

import java.util.List;
import java.util.Optional;

import dev.hltech.dredd.domain.ServiceVersion;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> findOne(ServiceVersion serviceVersion);

    List<ServiceContracts> findAllByServiceName(String serviceName);

    String getService(String name);

    List<String> getServiceNames();

}

package com.hltech.judged.server.domain.contracts;

import java.util.List;
import java.util.Optional;

import com.hltech.judged.server.domain.ServiceVersion;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> findOne(ServiceVersion serviceVersion);

    List<ServiceContracts> findAllByServiceName(String serviceName);

    String getService(String name);

    List<String> getServiceNames();

}

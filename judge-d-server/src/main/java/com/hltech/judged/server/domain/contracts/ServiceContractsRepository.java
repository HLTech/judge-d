package com.hltech.judged.server.domain.contracts;

import java.util.List;
import java.util.Optional;

import com.hltech.judged.server.domain.ServiceId;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> findOne(ServiceId serviceId);

    List<ServiceContracts> findAllByServiceName(String serviceName);

    Optional<Contract> findCapabilityByServiceIdProtocol(ServiceId serviceId, String protocol);

    String getService(String name);

    List<String> getServiceNames();

}

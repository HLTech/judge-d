package com.hltech.judged.server.domain.contracts

import com.google.common.collect.Maps
import com.hltech.judged.server.domain.ServiceId

import javax.persistence.NoResultException

class InMemoryServiceContractsRepository implements ServiceContractsRepository {

    private Map<ServiceId, ServiceContracts> storage = Maps.newHashMap()

    @Override
    ServiceContracts persist(ServiceContracts service) {
        storage.put(new ServiceId(service.name, service.version), service)
        return service
    }

    @Override
    Optional<ServiceContracts> findOne(ServiceId serviceId) {
        return Optional.ofNullable(storage.get(serviceId))
    }

    @Override
    String getService(String name)  {
        try {
            return storage
                .keySet()
                .grep {it.name == name}
                .first().name
        } catch (NoSuchElementException ex) {
            throw new NoResultException("Not found")
        }

    }

    @Override
    List<ServiceContracts> findAllByServiceName(String name) {
        return storage.entrySet()
            .stream()
            .filter { it -> it.getKey().name == name }
            .map { it -> it.value }
            .collect()
    }

    @Override
    List<String> getServiceNames() {
        return storage.keySet()
            .stream()
            .map { it -> it.name }
            .collect()
    }
}

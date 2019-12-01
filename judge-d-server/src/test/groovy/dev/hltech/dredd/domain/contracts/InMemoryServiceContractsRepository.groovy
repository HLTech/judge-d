package dev.hltech.dredd.domain.contracts

import com.google.common.collect.Maps
import dev.hltech.dredd.domain.ServiceVersion

import javax.persistence.NoResultException

class InMemoryServiceContractsRepository implements ServiceContractsRepository {

    private Map<ServiceVersion, ServiceContracts> storage = Maps.newHashMap()

    @Override
    ServiceContracts persist(ServiceContracts service) {
        storage.put(new ServiceVersion(service.name, service.version), service)
        return service;
    }

    @Override
    Optional<ServiceContracts> findOne(ServiceVersion serviceVersion) {
        return Optional.ofNullable(storage.get(serviceVersion))
    }

    @Override
    String getService(String name)  {
        try {
            return storage
                .keySet()
                .grep {it.name == name}
                .first().name
        } catch (NoSuchElementException nsee) {
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

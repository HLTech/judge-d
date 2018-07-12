package dev.hltech.dredd.domain.contracts

import com.google.common.collect.Maps
import dev.hltech.dredd.domain.environment.EnvironmentAggregate

class InMemoryServiceContractsRepository implements ServiceContractsRepository {

    private Map<EnvironmentAggregate.ServiceVersion, ServiceContracts> storage = Maps.newHashMap()

    @Override
    ServiceContracts persist(ServiceContracts service) {
        storage.put(new EnvironmentAggregate.ServiceVersion(service.name, service.version), service)
        return service;
    }

    @Override
    Optional<ServiceContracts> find(String name, String version) {
        return Optional.ofNullable(storage.get(new EnvironmentAggregate.ServiceVersion(name, version)))
    }
}

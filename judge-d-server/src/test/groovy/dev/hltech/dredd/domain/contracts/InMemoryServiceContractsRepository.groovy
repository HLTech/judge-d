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

    @Override
    List<ServiceContracts> find(String name) {
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

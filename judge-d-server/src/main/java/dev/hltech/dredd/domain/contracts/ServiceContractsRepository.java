package dev.hltech.dredd.domain.contracts;

import java.util.Optional;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> find(String name, String version);

}

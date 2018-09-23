package dev.hltech.dredd.domain.contracts;

import java.util.List;
import java.util.Optional;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> find(String name, String version);

    List<ServiceContracts> find(String name);

    List<String> getServiceNames();

}

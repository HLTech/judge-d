package dev.hltech.dredd.domain.contracts;

import org.hibernate.annotations.NotFound;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

public interface ServiceContractsRepository {

    ServiceContracts persist(ServiceContracts serviceContracts);

    Optional<ServiceContracts> find(String name, String version);

    String getService(String name) throws EntityNotFoundException;

    List<ServiceContracts> find(String name);

    List<String> getServiceNames();

}

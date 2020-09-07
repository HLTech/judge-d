package com.hltech.judged.server.infrastructure.persistence.contracts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface ServiceContractsTupleRepository extends JpaRepository<ServiceContractsTuple, ServiceVersion> {

    Optional<ServiceContractsTuple> findById_NameAndId_Version(String name, String version);
    @Query(value = "SELECT DISTINCT sc.id.name FROM ServiceContractsTuple sc")
    List<String> findId_NameDistinct();
    List<ServiceContractsTuple> findById_Name(String name);
}

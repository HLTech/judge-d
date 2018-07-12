package dev.hltech.dredd.domain.contracts;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataServicesRepository extends JpaRepository<ServiceContracts, ServiceContracts.ServiceContractsId> {
}

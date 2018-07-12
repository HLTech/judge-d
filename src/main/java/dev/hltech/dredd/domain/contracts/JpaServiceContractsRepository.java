package dev.hltech.dredd.domain.contracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class JpaServiceContractsRepository implements ServiceContractsRepository {

    private final SpringDataServicesRepository springDataServicesRepository;

    @Autowired
    public JpaServiceContractsRepository(SpringDataServicesRepository springDataServicesRepository) {
        this.springDataServicesRepository = springDataServicesRepository;
    }

    @Override
    public ServiceContracts persist(ServiceContracts serviceContracts) {
        return springDataServicesRepository.saveAndFlush(serviceContracts);
    }

    @Override
    public Optional<ServiceContracts> find(String name, String version) {
        return ofNullable(springDataServicesRepository.findOne(new ServiceContracts.ServiceContractsId(name, version)));
    }

}

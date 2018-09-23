package dev.hltech.dredd.domain.contracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class JpaServiceContractsRepository implements ServiceContractsRepository {

    private final EntityManager entityManager;

    @Autowired
    public JpaServiceContractsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ServiceContracts persist(ServiceContracts serviceContracts) {
        return entityManager.merge(serviceContracts);
    }

    @Override
    public Optional<ServiceContracts> find(String name, String version) {
        return ofNullable(entityManager.find(ServiceContracts.class, new ServiceContracts.ServiceContractsId(name, version)));
    }

    @Override
    public List<ServiceContracts> find(String name) {
        return entityManager
            .createQuery("select o from " + ServiceContracts.class.getName() + " o where o.id.name = :name", ServiceContracts.class)
            .setParameter("name", name)
            .getResultList();
    }

    @Override
    public List<String> getServiceNames() {
        return entityManager
            .createQuery("select o.id.name from " + ServiceContracts.class.getName() + " o", String.class)
            .getResultList();
    }

}

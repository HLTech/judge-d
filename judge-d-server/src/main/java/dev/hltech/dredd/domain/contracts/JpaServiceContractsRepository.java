package dev.hltech.dredd.domain.contracts;

import dev.hltech.dredd.domain.ServiceVersion;
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
    public Optional<ServiceContracts> findOne(ServiceVersion serviceVersion) {
        return ofNullable(entityManager.find(
            ServiceContracts.class,
            new ServiceVersion(serviceVersion.getName(), serviceVersion.getVersion())
        ));
    }

    @Override
    public String getService(String name) {
        return entityManager
            .createQuery("select distinct o.id.name from " + ServiceContracts.class.getName() + " o where o.id.name = :name", String.class)
            .setParameter("name", name)
            .getSingleResult();
    }

    @Override
    public List<ServiceContracts> findAllByServiceName(String name) {
        return entityManager
            .createQuery("select o from " + ServiceContracts.class.getName() + " o where o.id.name = :name", ServiceContracts.class)
            .setParameter("name", name)
            .getResultList();
    }

    @Override
    public List<String> getServiceNames() {
        return entityManager
            .createQuery("select distinct o.id.name from " + ServiceContracts.class.getName() + " o", String.class)
            .getResultList();
    }

}

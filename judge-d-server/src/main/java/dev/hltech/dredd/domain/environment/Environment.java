package dev.hltech.dredd.domain.environment;

import java.util.Collection;

public interface Environment {

    Collection<Service> getAllServices();

    Collection<Service> findServices(String serviceName);

}

package dev.hltech.dredd.domain.environment;

import java.util.Collection;

public interface ServiceDiscovery {

    Collection<Service> find(String serviceName);
}

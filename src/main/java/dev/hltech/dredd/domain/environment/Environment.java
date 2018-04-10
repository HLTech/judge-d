package dev.hltech.dredd.domain.environment;

import java.util.Collection;

public class Environment {

    private ServiceDiscovery serviceDiscovery;

    public Environment(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public Collection<Service> findServices(String serviceName){
        return serviceDiscovery.find(serviceName);
    }

}

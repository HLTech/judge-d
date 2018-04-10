package dev.hltech.dredd.domain.environment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Optional;

/**
 * this is here only until someone implements proper Kubernetes-based service discovery tool. Then, it will be moved to test classes
 */
public class MockServiceDiscovery implements ServiceDiscovery {

    private Multimap<String, Service> availableServices = HashMultimap.create();

    private MockServiceDiscovery(Multimap<String, Service> services) {
        this.availableServices.putAll(services);
    }

    @Override
    public Collection<Service> find(String serviceName) {
        return availableServices.get(serviceName);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private Multimap<String, Service> availableServices = HashMultimap.create();

        public Builder withProvider(String name, String swagger){
            availableServices.put(name, new Service() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public Optional<Provider> asProvider() {
                    return Optional.of(() -> swagger);
                }

            });
            return this;
        }


        public MockServiceDiscovery build(){
            return new MockServiceDiscovery(availableServices);
        }

    }


}

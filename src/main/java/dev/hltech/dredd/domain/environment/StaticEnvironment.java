package dev.hltech.dredd.domain.environment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.io.ByteStreams.toByteArray;

/**
 * this is here only until someone implements proper Kubernetes-based or db-based one. Then, it will be moved to test classes
 */
public class StaticEnvironment implements Environment {

    private Multimap<String, Service> availableServices = HashMultimap.create();

    private StaticEnvironment(Multimap<String, Service> services) {
        this.availableServices.putAll(services);
    }

    @Override
    public Collection<Service> findServices(String serviceName) {
        return availableServices.get(serviceName);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private Multimap<String, Service> availableServices = HashMultimap.create();

        public Builder withProvider(String name, String version, String swaggerResource){
            final String swagger;
            try {
                swagger = new String(toByteArray(getClass().getResourceAsStream(swaggerResource)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            availableServices.put(name, new Service() {

                @Override
                public String getVersion(){
                    return version;
                }

                @Override
                public Optional<Provider> asProvider() {
                    return Optional.of(new Provider() {
                        @Override
                        public String getVersion() {
                            return version;
                        }

                        @Override
                        public String getSwagger() {
                            return swagger;
                        }
                    });
                }

            });
            return this;
        }


        public StaticEnvironment build(){
            return new StaticEnvironment(availableServices);
        }

    }


}

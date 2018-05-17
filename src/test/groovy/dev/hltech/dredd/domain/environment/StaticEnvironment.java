package dev.hltech.dredd.domain.environment;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.ByteStreams.toByteArray;

public class StaticEnvironment implements Environment {

    private Multimap<String, Service> availableServices = HashMultimap.create();

    private StaticEnvironment(Multimap<String, Service> services) {
        this.availableServices.putAll(services);
    }

    @Override
    public Collection<Service> findServices(String serviceName) {
        return availableServices.get(serviceName);
    }

    @Override
    public Collection<Service> getAllServices() {
        return availableServices.values();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private Multimap<String, Service> availableServices = HashMultimap.create();

        public Builder withProvider(String name, String version, InputStream inputStream) throws IOException {
            return withProvider(name, version, new String(toByteArray(inputStream)));
        }

        public Builder withProvider(String name, String version, String swagger){
            availableServices.put(name, new Service() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getVersion(){
                    return version;
                }

                @Override
                public Provider asProvider() {
                    return () -> Optional.ofNullable(swagger);
                }

                @Override
                public Consumer asConsumer() {
                    return providerName -> {
                        if (null == providerName) {
                            throw new KubernetesEnvironmentException("Wrong provider name");
                        }
                        return Optional.empty();
                    };
                }
            });
            return this;
        }

        public StaticEnvironment build(){
            return new StaticEnvironment(availableServices);
        }

        public Builder withConsumer(String name, String version, Collection<RequestResponsePact> pacts) {
            availableServices.put(name, new Service() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getVersion() {
                    return version;
                }

                @Override
                public Consumer asConsumer(){
                    return providerName -> pacts.stream().filter(pact -> pact.getProvider().getName().equals(providerName)).findFirst();
                }

                @Override
                public Provider asProvider() {
                    return Optional::empty;
                }

            });
            return this;
        }
    }


}

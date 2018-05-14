package dev.hltech.dredd.domain.environment;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hltech.dredd.integration.kubernetes.PodClient;
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient;
import feign.Feign;
import feign.Target;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static au.com.dius.pact.model.PactReader.loadPact;

@Slf4j
public class KubernetesEnvironment implements Environment {

    private static final Integer DEFAULT_CONTAINER_PORT = 9999;

    private KubernetesClient kubernetesClient;
    private PactBrokerClient pactBrokerClient;
    private ObjectMapper objectMapper;
    private Feign feign;

    public KubernetesEnvironment(KubernetesClient kubernetesClient,
                                 PactBrokerClient pactBrokerClient,
                                 ObjectMapper objectMapper,
                                 Feign feign) {
        this.kubernetesClient = kubernetesClient;
        this.pactBrokerClient = pactBrokerClient;
        this.objectMapper = objectMapper;
        this.feign = feign;
    }

    @Override
    public Collection<Service> getAllServices() {
        return getPods().stream()
            .map(pod -> {
                try{
                   return createService(pod);
                }
                catch(KubernetesEnvironmentException exception) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Service> findServices(String serviceName) {
        return getPods(serviceName).stream()
            .map(pod -> {
                try{
                    return createService(pod);
                }
                catch(KubernetesEnvironmentException exception) {
                    log.debug("No service resolved for pod: {}", pod.getMetadata().getName());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Service createService(Pod pod) {
        try {
            String podName = getPodName(pod);
            String podIP = getPodIP(pod);
            Integer podPort = getPodPort(pod).orElse(DEFAULT_CONTAINER_PORT);
            PodClient podClient = feign.newInstance(
                new Target.HardCodedTarget<>(PodClient.class, "http://" + podIP + ":" + podPort));
            String podVersion = getPodVersion(podClient);

            return new Service() {
                @Override
                public String getName() {
                    return podName;
                }

                @Override
                public String getVersion() {
                    return podVersion;
                }

                @Override
                public Provider asProvider() {
                    return new Provider() {
                        @Override
                        public Optional<String> getSwagger() {
                            try {
                                return Optional.ofNullable(podClient.getSwagger(URI.create(podName)));
                            } catch (Exception ex) {
                                log.debug("Swagger not fetched, pod: name- {}, version - {}", podName, podVersion);
                                return Optional.empty();
                            }
                        }
                    };
                }

                @Override
                public Consumer asConsumer() {
                    return new Consumer() {
                        @Override
                        public Optional<RequestResponsePact> getPact(String providerName) {
                            try {
                                ObjectNode pact = pactBrokerClient.getPact(providerName, podName, podVersion);
                                return Optional.ofNullable(
                                    (RequestResponsePact) loadPact(objectMapper.writeValueAsString(pact)));
                            } catch (Exception ex) {
                                log.debug("Pact not fetched, pod: name- {}, version - {}", podName, podVersion);
                                return Optional.empty();
                            }
                        }
                    };
                }
            };
        }
        catch(Exception exception) {
            log.debug("Exception during service resolution for pod: {}", pod.getMetadata().getName());
            throw new KubernetesEnvironmentException("Exception during service resolution", exception);
        }
    }

    private Collection<Pod> getPods(String name) {
        return kubernetesClient.pods().inAnyNamespace().withLabel("app", name).list().getItems();
    }

    private Collection<Pod> getPods() {
        return kubernetesClient.pods().inAnyNamespace().list().getItems();
    }

    private String getPodIP(Pod pod) {
        return pod.getStatus().getPodIP();
    }

    private Optional<Integer> getPodPort(Pod pod) {
        Optional<Container> container = pod.getSpec().getContainers().stream()
            .filter(cont -> cont.getName().equals(getPodName(pod)))
            .findFirst();

        if (!container.isPresent()) {
            return Optional.empty();
        }

        Optional<ContainerPort> port = container.get().getPorts().stream()
            .filter(containerPort -> "monitoring".equals(containerPort.getName()))
            .findFirst();

        if (!port.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(port.get().getContainerPort());
    }

    private String getPodName(Pod pod) {
        return pod
            .getMetadata()
            .getLabels()
            .get("app");
    }

    private String getPodVersion(PodClient podClient) {
        JsonNode response = podClient.getInfo();
        return response.get("build").get("version").asText();
    }
}

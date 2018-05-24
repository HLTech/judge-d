package dev.hltech.dredd.domain.environment;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hltech.dredd.integration.kubernetes.PodClient;
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient;
import feign.Feign;
import feign.FeignException;
import feign.Target;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static au.com.dius.pact.model.PactReader.loadPact;

@Slf4j
public class KubernetesEnvironment implements Environment {

    private static final Integer DEFAULT_CONTAINER_VERSION_PORT = 9999;
    private static final Integer DEFAULT_CONTAINER_API_PORT = 8080;

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
            Integer podVersionPort = getPodVersionPort(pod).orElse(DEFAULT_CONTAINER_VERSION_PORT);
            String podVersion = getPodVersion(podIP, podVersionPort);

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
                                Integer apiPort = getPodApiPort(pod).orElse(DEFAULT_CONTAINER_API_PORT);

                                PodClient podClient = feign.newInstance(
                                    new Target.HardCodedTarget<>(PodClient.class, "http://" + podIP + ":" + apiPort));

                                ResponseEntity<String> swaggerResponse = podClient.getSwagger(URI.create(podName));

                                validateResponseStatus(swaggerResponse.getStatusCode());

                                return Optional.ofNullable(swaggerResponse.getBody());
                            } catch (HttpStatusCodeException ex) {
                                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                                    return Optional.empty();
                                }
                                log.debug("Swagger not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during swagger resolution", ex);
                            } catch (FeignException ex) {
                                if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                                    return Optional.empty();
                                }
                                log.debug("Swagger not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during swagger resolution", ex);
                            } catch (Exception ex) {
                                log.debug("Swagger not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during swagger resolution", ex);
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
                                ResponseEntity<ObjectNode> pactResponse = pactBrokerClient.getPact(
                                    providerName, podName, podVersion);

                                validateResponseStatus(pactResponse.getStatusCode());

                                ObjectNode pact = pactResponse.getBody();

                                return Optional.ofNullable(
                                    (RequestResponsePact) loadPact(objectMapper.writeValueAsString(pact.get("body"))));
                            } catch (HttpStatusCodeException ex) {
                                if(ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                                    return Optional.empty();
                                }
                                log.debug("Pact not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during pact resolution", ex);
                            } catch (FeignException ex) {
                                if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                                    return Optional.empty();
                                }
                                log.debug("Pact not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during pact resolution", ex);
                            } catch (Exception ex) {
                                log.debug("Pact not fetched, pod: name- {}, version - {}", podName, podVersion);
                                throw new KubernetesEnvironmentException("Exception during pact resolution", ex);
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

    private void validateResponseStatus(HttpStatus status) {
        if(status.is4xxClientError()) {
            log.error(String.format("Client request resulted in HTTP status code %d", status.value()));
            throw new HttpClientErrorException(status);
        }

        if(status.is5xxServerError()) {
            log.error(String.format("Server request resulted in HTTP status code %d", status.value()));
            throw new HttpServerErrorException(status);
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

    private Optional<Integer> getPodVersionPort(Pod pod) {
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

    private Optional<Integer> getPodApiPort(Pod pod) {
        Optional<Container> container = pod.getSpec().getContainers().stream()
            .filter(cont -> cont.getName().equals(getPodName(pod)))
            .findFirst();

        if (!container.isPresent()) {
            return Optional.empty();
        }

        Optional<ContainerPort> port = container.get().getPorts().stream()
            .filter(containerPort -> "api".equals(containerPort.getName()))
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

    private String getPodVersion(String podIP, Integer podVersionPort) {
        PodClient podClient = feign.newInstance(
            new Target.HardCodedTarget<>(PodClient.class, "http://" + podIP + ":" + podVersionPort));

        return podClient.getInfo().getBody().get("build").get("version").asText();
    }
}

package dev.hltech.dredd.domain.environment;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesEnvironment implements Environment {

    private static final String VERSION_ENDPOINT = "http://%s:%d/info";
    private static final Integer DEFAULT_CONTAINER_PORT = 9999;

    private KubernetesClient kubernetesClient;
    private RestTemplate restTemplate;

    public KubernetesEnvironment(KubernetesClient kubernetesClient, RestTemplate restTemplate) {
        this.kubernetesClient = kubernetesClient;
        this.restTemplate = restTemplate;
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
                    log.debug("No service resolved for pod: {}", pod);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Service createService(Pod pod) {
        try {
            String podName = getPodName(pod);
            String podVersion = getPodVersion(getPodIP(pod), getPodPort(pod).orElse(DEFAULT_CONTAINER_PORT));
            return new Service() {
                @Override
                public String getName() {
                    return podName;
                }

                @Override
                public String getVersion() {
                    return podVersion;
                }
            };
        }
        catch(Exception exception) {
            log.debug("Exception during service resolution for pod: {}", pod);
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

    private String getPodVersion(String ip, Integer port) {
        JsonNode response = restTemplate.getForObject(String.format(VERSION_ENDPOINT, ip, port), JsonNode.class);
        return response.get("build").get("version").asText();
    }
}

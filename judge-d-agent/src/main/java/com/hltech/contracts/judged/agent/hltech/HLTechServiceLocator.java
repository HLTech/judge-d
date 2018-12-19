package com.hltech.contracts.judged.agent.hltech;

import com.hltech.contracts.judged.agent.ServiceLocator;
import feign.Feign;
import feign.Target;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HLTechServiceLocator implements ServiceLocator {

    private static final Integer DEFAULT_CONTAINER_VERSION_PORT = 9999;
    private static final String EXCLUDE_FROM_JURISDICTION_LABEL = "exclude-from-judged-jurisdiction";
    private static final Logger LOGGER = LoggerFactory.getLogger(HLTechServiceLocator.class);

    private final KubernetesClient kubernetesClient;
    private Feign feign;

    public HLTechServiceLocator(KubernetesClient kubernetesClient, Feign feign) {
        this.kubernetesClient = kubernetesClient;
        this.feign = feign;
    }

    @Override
    public Set<Service> locateServices() {
        return getPods().stream()
            .filter(pod -> !isExcludedFromJurisdiction(pod))
            .map(pod -> {
                try{
                    return createService(pod);
                }
                catch(KubernetesEnvironmentException exception) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private boolean isExcludedFromJurisdiction(Pod pod) {
        return pod.getMetadata().getLabels().get(EXCLUDE_FROM_JURISDICTION_LABEL) != null
            && pod.getMetadata().getLabels().get(EXCLUDE_FROM_JURISDICTION_LABEL).equals("true");
    }

    private Collection<Pod> getPods() {
        return kubernetesClient.pods().inAnyNamespace().list().getItems();
    }

    private Service createService(Pod pod) {
        try {
            String podName = getPodName(pod);
            String podIP = getPodIP(pod);
            Integer podVersionPort = getPodVersionPort(pod).orElse(DEFAULT_CONTAINER_VERSION_PORT);
            String podVersion = getPodVersion(podIP, podVersionPort);

            return new Service(podName, podVersion);
        }
        catch(Exception exception) {
            if(pod.getMetadata() == null || pod.getMetadata().getName() == null) {
                LOGGER.debug("Exception during service resolution");
                throw new KubernetesEnvironmentException("Exception during service resolution", exception);
            }

            LOGGER.debug("Exception during service resolution for pod: {}", pod.getMetadata().getName());
            throw new KubernetesEnvironmentException("Exception during service resolution", exception);
        }
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

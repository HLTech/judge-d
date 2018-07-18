package com.hltech.contracts.judged.agent.k8s;

import com.hltech.contracts.judged.agent.JudgeDPublisher;
import com.hltech.contracts.judged.agent.ServiceLocator;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class K8sLabelBasedServiceLocator implements ServiceLocator {

    private final KubernetesClient kubernetesClient;
    private String requiredLabel;

    public K8sLabelBasedServiceLocator(KubernetesClient kubernetesClient, String requiredLabel) {
        this.kubernetesClient = kubernetesClient;
        this.requiredLabel = requiredLabel;
    }

    @Override
    public Set<JudgeDPublisher.ServiceForm> locateServices() {
        Set<JudgeDPublisher.ServiceForm> services = kubernetesClient.pods().inAnyNamespace().withLabel(requiredLabel).list().getItems()
            .stream()
            .filter(pod -> "running".equalsIgnoreCase(pod.getStatus().getPhase()))
            .flatMap(pod -> pod.getSpec().getContainers().stream())
            .map(container -> {
                if (container.getImage().contains(":")) {
                    String imageName = container.getImage().split(":")[0];
                    String imageVersion = container.getImage().split(":")[1];

                    return Optional.of(new JudgeDPublisher.ServiceForm(
                        imageName.contains("/") ? imageName.substring(imageName.lastIndexOf("/")+1) : imageName,
                        imageVersion
                    ));
                } else {
                    return Optional.<JudgeDPublisher.ServiceForm>empty();
                }
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
        return services;
    }

}

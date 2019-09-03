package com.hltech.contracts.judged.agent.k8s;

import com.hltech.contracts.judged.agent.ServiceLocator;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class K8sLabelBasedServiceLocator implements ServiceLocator {

    private static final String EXCLUDE_FROM_JURISDICTION_LABEL = "exclude-from-judged-jurisdiction";

    private final KubernetesClient kubernetesClient;
    private final String requiredLabel;
    private final Predicate<Namespace> excludedNamespacesFilter;
    private final Predicate<Namespace> includedNamespacesFilter;

    public K8sLabelBasedServiceLocator(
        KubernetesClient kubernetesClient,
        String requiredLabel,
        Set<String> excludedNamespaces,
        Set<String> includedNamespaces
    ) {
        this.kubernetesClient = kubernetesClient;
        this.requiredLabel = requiredLabel;
        this.excludedNamespacesFilter = excludedNamespaces.isEmpty()
            ? s -> true
            : s -> !excludedNamespaces.contains(s.getMetadata().getName());
        this.includedNamespacesFilter = includedNamespaces.isEmpty()
            ? s -> true
            : s -> includedNamespaces.contains(s.getMetadata().getName());
    }

    @Override
    public Set<Service> locateServices() {
        List<String> namespacesToScan = kubernetesClient
            .namespaces().list().getItems().stream().filter(excludedNamespacesFilter).filter(includedNamespacesFilter)
            .map(n -> n.getMetadata().getName())
            .collect(toList());

        return kubernetesClient.pods().inAnyNamespace().withLabel(requiredLabel).list().getItems()
            .stream()
            .filter(d -> namespacesToScan.contains(d.getMetadata().getNamespace()))
            .filter(pod -> !isExcludedFromJurisdiction(pod))
            .filter(pod -> "running".equalsIgnoreCase(pod.getStatus().getPhase()))
            .flatMap(pod -> pod.getSpec().getContainers().stream())
            .map(container -> {
                if (container.getImage().contains(":")) {
                    String imageName = container.getImage().split(":")[0];
                    String imageVersion = container.getImage().split(":")[1];

                    return Optional.of(new Service(
                        imageName.contains("/") ? imageName.substring(imageName.lastIndexOf("/") + 1) : imageName,
                        imageVersion
                    ));
                } else {
                    return Optional.<Service>empty();
                }
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    private boolean isExcludedFromJurisdiction(Pod pod) {
        return pod.getMetadata().getLabels().get(EXCLUDE_FROM_JURISDICTION_LABEL) != null
            && pod.getMetadata().getLabels().get(EXCLUDE_FROM_JURISDICTION_LABEL).equals("true");
    }

}

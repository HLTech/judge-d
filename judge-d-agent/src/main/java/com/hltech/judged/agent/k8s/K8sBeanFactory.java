package com.hltech.judged.agent.k8s;

import com.hltech.judged.agent.ServiceLocator;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile("kubernetes")
class K8sBeanFactory {

    @Bean
    KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @Bean
    ServiceLocator serviceLocator(
        KubernetesClient kubernetesClient,
        @Value("${hltech.contracts.judge-d.requiredLabel}") String requiredLabel,
        @Value("${hltech.contracts.judge-d.excluded-namespaces:#{''}}") Set<String> excludedNamespaces,
        @Value("${hltech.contracts.judge-d.included-namespaces:#{''}}") Set<String> includedNamespaces

    ) {
        return new K8sLabelBasedServiceLocator(kubernetesClient, requiredLabel, excludedNamespaces, includedNamespaces);
    }

}

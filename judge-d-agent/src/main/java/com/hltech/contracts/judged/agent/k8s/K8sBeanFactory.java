package com.hltech.contracts.judged.agent.k8s;

import com.hltech.contracts.judged.agent.ServiceLocator;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile("kubernetes")
public class K8sBeanFactory {

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @Bean
    public ServiceLocator serviceLocator(
        KubernetesClient kubernetesClient,
        @Value("${hltech.contracts.judge-d.requiredLabel}") String requiredLabel,
        @Value("${hltech.contracts.judge-d.excluded-namespaces:#{''}}") Set<String> excludedNamespaces,
        @Value("${hltech.contracts.judge-d.included-namespaces:#{''}}") Set<String> includedNamespaces

    ) {
        return new K8sLabelBasedServiceLocator(kubernetesClient, requiredLabel, excludedNamespaces, includedNamespaces);
    }

}

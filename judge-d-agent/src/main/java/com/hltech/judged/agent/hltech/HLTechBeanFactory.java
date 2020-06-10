package com.hltech.judged.agent.hltech;

import com.hltech.judged.agent.ServiceLocator;
import feign.Feign;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("hltech")
public class HLTechBeanFactory {

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @Bean
    public ServiceLocator serviceLocator(KubernetesClient kubernetesClient, Feign feign) {
        return new HLTechServiceLocator(kubernetesClient, feign);
    }

}

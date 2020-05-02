package com.hltech.contracts.judged.agent.consul;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("consul")
public class ConsulBeanFactory {

    @Bean
    ConsulClient consulAgentClient(@Value("${hltech.contracts.judge-d.consul-host}") String consulUrl) {
        return new ConsulClient(consulUrl);
    }

    @Bean
    ConsulTagBasedServiceLocator consulServiceLocator(ConsulClient discoveryClient) {
        return new ConsulTagBasedServiceLocator(discoveryClient);
    }
}

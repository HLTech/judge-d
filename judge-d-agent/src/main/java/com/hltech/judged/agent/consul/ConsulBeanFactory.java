package com.hltech.judged.agent.consul;

import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.HealthConsulClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("consul")
@RequiredArgsConstructor
public class ConsulBeanFactory {

    @Value("${hltech.contracts.judge-d.consul-host}")
    private String consulUrl;

    @Bean
    CatalogClient catalogConsulClient() {
        return new CatalogConsulClient(consulUrl);
    }

    @Bean
    HealthClient healthConsulClient() {
        return new HealthConsulClient(consulUrl);
    }

    @Bean
    ConsulTagBasedServiceLocator consulServiceLocator(CatalogClient catalogConsulClient,
                                                      HealthClient healthConsulClient) {
        return new ConsulTagBasedServiceLocator(catalogConsulClient, healthConsulClient);
    }
}

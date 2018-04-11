package dev.hltech.dredd.config;

import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.MockServiceDiscovery;
import dev.hltech.dredd.domain.environment.ServiceDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static com.google.common.io.ByteStreams.toByteArray;

@Configuration
public class BeanFactory {

    @Bean
    public Environment environment(ServiceDiscovery serviceDiscovery){
        return new Environment(serviceDiscovery);
    }

    @Bean
    public ServiceDiscovery serviceDiscovery() throws IOException {
        return  MockServiceDiscovery.builder()
            .withProvider(
                "dde-instruction-gateway",
                new String(toByteArray(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")))
            )
            .build();
    }

}

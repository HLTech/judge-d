package dev.hltech.dredd.config;

import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Lists;
import dev.hltech.dredd.domain.PactValidator;
import dev.hltech.dredd.domain.SwaggerValidator;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.StaticEnvironment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static au.com.dius.pact.model.PactReader.loadPact;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.ByteStreams.toByteArray;

@Configuration
public class BeanFactory {

    @Bean
    public Environment hlEnvironment() throws IOException {
        return StaticEnvironment.builder()
            .withProvider(
                "dde-instruction-gateway",
                "1.0",
                new String(toByteArray(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")))
            )
            .withConsumer(
                "frontend",
                "1.0",
                newArrayList(
                    (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
                )
            )
            .build();
    }

    @Bean
    public PactValidator pactValidator(Environment environment){
        return new PactValidator(environment);
    }

    @Bean
    public SwaggerValidator swaggerValidator(Environment environment) {
        return new SwaggerValidator(environment);
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        restTemplate.setMessageConverters(Lists.newArrayList(converter));
        return restTemplate;
    }

}

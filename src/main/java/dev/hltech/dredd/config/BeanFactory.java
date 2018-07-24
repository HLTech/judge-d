package dev.hltech.dredd.config;

import au.com.dius.pact.model.RequestResponsePact;
import dev.hltech.dredd.domain.PactValidator;
import dev.hltech.dredd.domain.SwaggerValidator;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.StaticEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                "backend-provider",
                "1.0",
                new String(toByteArray(getClass().getResourceAsStream("/backend-provider-swagger.json")))
            )
            .withConsumer(
                "frontend",
                "1.0",
                newArrayList(
                    (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
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

}

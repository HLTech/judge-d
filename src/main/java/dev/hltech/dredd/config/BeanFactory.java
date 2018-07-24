package dev.hltech.dredd.config;

import dev.hltech.dredd.domain.PactValidator;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.StaticEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

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
            .build();
    }

    @Bean
    public PactValidator pactValidator(Environment environment){
        return new PactValidator(environment);
    }

}

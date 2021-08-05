package com.hltech.judged.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.util.Json;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class BeanFactory {

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //According to issue https://bitbucket.org/atlassian/swagger-request-validator/issues/331/timestamp-iso8601-format-issue
        Json.mapper().registerModule(javaTimeModule);

        ObjectMapper objectMapper = mapperBuilder
            .modules(javaTimeModule)
            .build();

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}

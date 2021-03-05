package com.hltech.judged.server

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class PostgresPropertiesInitializer extends PostgresDatabaseSpecification implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues
            .of("spring.datasource.url=${postgres.jdbcUrl}")
            .and("spring.datasource.username=${postgres.username}")
            .and("spring.datasource.password=${postgres.password}")
            .and("spring.datasource.platform=postgres")
            .applyTo(configurableApplicationContext)
    }
}

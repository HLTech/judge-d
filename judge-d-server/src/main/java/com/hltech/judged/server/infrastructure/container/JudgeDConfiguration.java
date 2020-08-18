package com.hltech.judged.server.infrastructure.container;

import com.hltech.judged.server.domain.JudgeDApplicationService;
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import com.hltech.judged.server.domain.validation.jms.JmsContractValidator;
import com.hltech.judged.server.domain.validation.rest.RestContractValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JudgeDConfiguration {

    @Bean
    JudgeDApplicationService judgeDApplicationService(EnvironmentRepository environmentRepository,
                                                      ServiceContractsRepository serviceContractsRepository) {
        return new JudgeDApplicationService(environmentRepository, serviceContractsRepository);
    }

    @Bean
    JmsContractValidator jmsContractValidator() {
        return new JmsContractValidator();
    }

    @Bean
    RestContractValidator restContractValidator() {
        return new RestContractValidator();
    }
}

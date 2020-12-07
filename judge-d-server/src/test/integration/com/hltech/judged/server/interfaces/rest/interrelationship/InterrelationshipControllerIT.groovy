package com.hltech.judged.server.interfaces.rest.interrelationship

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(InterrelationshipController.class)
@ActiveProfiles("test-integration")
class InterrelationshipControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    com.hltech.judged.server.domain.environment.EnvironmentRepository environmentRepository

    @Autowired
    MockMvc mockMvc

    def "should return 200 when getting interrelationship for any environment"() {
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/interrelationship/SIT'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<InterrelationshipDto>() {}) != null
    }

    @TestConfiguration
    static class TestConfig extends com.hltech.judged.server.config.BeanFactory {

        @Bean
        com.hltech.judged.server.domain.environment.EnvironmentRepository environmentRepository() {
            return new com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository()
        }

        @Bean
        com.hltech.judged.server.domain.contracts.ServiceContractsRepository serviceContractsRepository() {
            return new com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository()
        }

        @Bean
        com.hltech.judged.server.interfaces.rest.contracts.ContractsMapper contractsMapper() {
            return new com.hltech.judged.server.interfaces.rest.contracts.ContractsMapper()
        }
    }
}

package dev.hltech.dredd.interfaces.rest.interrelationship

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.interfaces.rest.contracts.ContractsMapper
import dev.hltech.dredd.interfaces.rest.environment.ServiceDto
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
    EnvironmentRepository environmentRepository

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
    static class TestConfig extends BeanFactory {

        @Bean
        EnvironmentRepository environmentRepository() {
            return new InMemoryEnvironmentRepository()
        }

        @Bean
        ServiceContractsRepository serviceContractsRepository() {
            return new InMemoryServiceContractsRepository()
        }

        @Bean
        ContractsMapper contractsMapper() {
            return new ContractsMapper()
        }
    }
}

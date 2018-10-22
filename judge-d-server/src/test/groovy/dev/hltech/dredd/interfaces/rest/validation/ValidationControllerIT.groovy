package dev.hltech.dredd.interfaces.rest.validation

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.JudgeD
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.domain.validation.InterfaceContractValidator
import dev.hltech.dredd.domain.validation.ping.PingContractValidator
import dev.hltech.dredd.interfaces.rest.environment.ServiceDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(ValidationController.class)
@ActiveProfiles("test-integration")
class ValidationControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServiceContractsRepository serviceContractsRepository

    @Autowired
    MockMvc mockMvc;

    def "should return 200 when validate given all went fine - old"() {
        given:
            serviceContractsRepository.persist(new ServiceContracts(
                "service-name",
                "1.0",
                [:] as Map,
                [:] as Map
            ))
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/environment-compatibility-report/SIT/service-name:1.0'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ServiceDto>>() {}) != null
    }

    def "should return 404 when validate service contracts against env given contracts have not been registered - old"() {
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/validation-report/environment/SIT/service/other-service:1.0'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 404
    }

    def "should return 200 when validate given all went fine"() {
        given:
            serviceContractsRepository.persist(new ServiceContracts(
                "service-name",
                "1.0",
                [:] as Map,
                [:] as Map
            ))
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/environment-compatibility-report/service-name:1.0?environment=SIT&environment=UAT'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ServiceDto>>() {}) != null
    }

    def "should return 404 when validate service contracts against env given contracts have not been registered"() {
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/validation-report/service/other-service:1.0?environment=SIT&environment=UAT'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 404
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
        JudgeD repository(EnvironmentRepository environmentRepository, ServiceContractsRepository serviceContractsRepository) {
            return new JudgeD(environmentRepository, serviceContractsRepository)
        }

        @Bean
        InterfaceContractValidator<String, String> validator() {
            return new PingContractValidator()
        }

    }
}

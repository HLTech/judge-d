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
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(ValidationController.class)
@ActiveProfiles("test-integration")
class ValidationControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ServiceContractsRepository serviceContractsRepository

    @Autowired
    MockMvc mockMvc

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
                get(new URI('/environment-compatibility-report/other-service:1.0?environment=SIT&environment=UAT'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 404
    }

    def "should return 200 when validate multiple services against env"(){
        given:
            def providerOld = serviceContractsRepository.persist(new ServiceContracts(
                "provider",
                "1.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            def consumerOld = serviceContractsRepository.persist(new ServiceContracts(
                "consumer",
                "1.0",
                [:],
                ["provider": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            def providerNew = serviceContractsRepository.persist(new ServiceContracts(
                "provider",
                "2.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            def consumerNew = serviceContractsRepository.persist(new ServiceContracts(
                "consumer",
                "2.0",
                [:],
                ["provider": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/environment-compatibility-report?services=provider:2.0&services=consumer:2.0&environment=SIT'))
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            def string = response.getContentAsString()
            objectMapper.readValue(string, new TypeReference<List<BatchValidationReportDto>>() {}) != null
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

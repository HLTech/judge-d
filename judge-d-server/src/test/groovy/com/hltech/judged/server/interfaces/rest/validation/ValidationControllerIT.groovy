package com.hltech.judged.server.interfaces.rest.validation

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hltech.judged.server.config.BeanFactory
import com.hltech.judged.server.domain.JudgeDApplicationService
import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.infrastructure.persistence.contracts.ServiceVersion
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.environment.EnvironmentRepository
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.validation.InterfaceContractValidator
import com.hltech.judged.server.domain.validation.ping.PingContractValidator
import com.hltech.judged.server.interfaces.rest.environment.ServiceDto
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
                new ServiceId('service-name', '1.0'),
                [],
                []
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
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider', '2.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '2.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
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
        JudgeDApplicationService repository(EnvironmentRepository environmentRepository, ServiceContractsRepository serviceContractsRepository) {
            return new JudgeDApplicationService(environmentRepository, serviceContractsRepository)
        }

        @Bean
        InterfaceContractValidator<String, String> validator() {
            return new PingContractValidator()
        }

    }
}

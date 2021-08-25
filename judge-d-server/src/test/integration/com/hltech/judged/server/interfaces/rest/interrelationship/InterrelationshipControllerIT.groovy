package com.hltech.judged.server.interfaces.rest.interrelationship

import com.fasterxml.jackson.databind.ObjectMapper
import com.hltech.judged.server.config.BeanFactory
import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.EnvironmentRepository
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.environment.Space
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(InterrelationshipController.class)
@ActiveProfiles("test")
class InterrelationshipControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    EnvironmentRepository environmentRepository

    @Autowired
    ServiceContractsRepository serviceContractsRepository

    @Autowired
    MockMvc mockMvc

    def "should return 200 when getting interrelationship for any environment"() {
        given: 'data saved in the repository'
            serviceContractsRepository.persist(createServiceContracts('1', '1'))

            environmentRepository.persist(new Environment('SIT', [new Space('def', [new ServiceId('1', '1')] as Set)] as Set))

        expect: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/interrelationship/SIT'))
                    .accept('application/json')
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType('application/json'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.environment').value('SIT'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].name').value('1'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].version').value('1'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].capabilities.jms.value').value('contract-jms'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].capabilities.jms.mimeType').value('json'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].expectations.prov.rest.value').value('contract-rest'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].expectations.prov.rest.mimeType').value('json'))
                .andExpect(MockMvcResultMatchers.jsonPath('$.serviceContracts[0].publicationTime').exists())
    }

    def "should return 404 when getting interrelationship for not existing environment"() {
        expect: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get(new URI('/interrelationship/unknown'))
                    .accept("application/json")
            )
                .andExpect(status().isNotFound())
    }

    def createServiceContracts(def name, def version) {
        new ServiceContracts(
            new ServiceId(name, version),
            [new Capability('jms', new Contract('contract-jms', 'json'))],
            [new Expectation('prov', 'rest', new Contract('contract-rest', 'json'))])
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
    }
}

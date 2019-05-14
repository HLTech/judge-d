package dev.hltech.dredd.interfaces.rest.contracts


import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static java.util.Collections.emptyMap
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(ServicesController.class)
@ActiveProfiles("test-integration")
class ServicesControllerIT extends Specification {
    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ServiceContractsRepository serviceContractsRepository


    def 'should successfully retrieve list of services'() {
        given:
            serviceRegisteredWithName("processor")
            serviceRegisteredWithName("manager")
        when:
            def response = mockMvc.perform(
                get('/services')
            ).andReturn().getResponse()
        then:
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            def object = new JsonSlurper().parseText(response.getContentAsString())
            assert object instanceof List
            assert object.toSorted() == ["manager", "processor"].toSorted()
    }

    def serviceRegisteredWithName(String serviceName) {
        ServiceContracts contracts = new ServiceContracts(serviceName, "2.0", emptyMap(), emptyMap())
        serviceContractsRepository.persist(contracts)
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        ServiceContractsRepository repository() {
            return new InMemoryServiceContractsRepository()
        }
    }
}

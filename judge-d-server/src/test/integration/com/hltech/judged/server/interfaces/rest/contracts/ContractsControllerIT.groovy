package com.hltech.judged.server.interfaces.rest.contracts

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hltech.judged.server.config.BeanFactory
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

@WebMvcTest(ContractsController.class)
@ActiveProfiles("test")
class ContractsControllerIT extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private MockMvc mockMvc

    def 'return 404 when no contracts registered for given service and version'() {
        when: 'rest validatePacts url is hit'
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            def response = mockMvc.perform(
                get(serviceNameVersionUrl(), serviceName, version)
                    .contentType("application/json")
            ).andReturn().getResponse()
        then: 'controller returns 404'
            response.getStatus() == 404
    }

    def 'return 404 when no contracts registered for given service'() {
        when: 'rest validatePacts url is hit'
            def serviceName = randomAlphabetic(10)
            def response = mockMvc.perform(
                get('/contracts/services/{serviceName}', serviceName)
                    .contentType("application/json")
            ).andReturn().getResponse()
        then: 'controller returns 404'
            response.getStatus() == 404
    }


    def 'should return 200 and json when create a service contracts'() {
        when: 'rest validatePacts url is hit'
            println objectMapper.writeValueAsString(randomServiceContractFormWithExpectationsAndCapabilities())
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            def response = mockMvc.perform(
                post(serviceNameVersionUrl(), serviceName, version)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(randomServiceContractFormWithExpectationsAndCapabilities()))
            ).andReturn().getResponse()
        then: 'controller returns dto response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<ServiceContractsDto>() {})
    }

    def 'should return 200 and json when get previously saved service contracts'() {
        given: 'rest validatePacts url is hit'
            def (serviceName, version) = createAService()
        when:
            def response = mockMvc.perform(
                get(serviceNameVersionUrl(), serviceName, version)
                    .contentType("application/json")
            ).andReturn().getResponse()
        then: 'controller returns dto response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<ServiceContractsDto>() {})
    }

    def 'should successfully retrieve list of services'() {
        when:
            def response = mockMvc.perform(
                get('/contracts/services')
            ).andReturn().getResponse()
        then:
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<String>>() {})
    }

    def 'should successfully retrieve details of a service'() {
        given:
            def serviceName = createAService().get(0)
        when:
            def response = mockMvc.perform(
                get('/contracts/services/{serviceName}', serviceName)
            ).andReturn().getResponse()
        then:
            response.getStatus() == 200
            response.getContentType().contains("text/plain")
            response.getContentAsString() == serviceName
    }

    def 'should calling /contracts redirect to /contracts/services to improve api discovery'() {
        when:
            def response = mockMvc.perform(
                get('/contracts')
            ).andReturn().getResponse()
        then:
            response.getStatus() == 302
            response.getRedirectedUrl() == "contracts/services"
    }

    def 'should successfully retrieve list of service versions'() {
        given:
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            mockMvc.perform(
                post(serviceNameVersionUrl(), serviceName, version)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(randomServiceContractFormWithExpectationsAndCapabilities()))
            ).andReturn().getResponse()
        when:
            def response = mockMvc.perform(
                get('/contracts/services/{serviceName}/versions', serviceName)
            ).andReturn().getResponse()
        then:
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<String>>() {})
    }

    def 'should successfully retrieve list of service capabilities for protocol'() {
        given:
            def serviceName = randomAlphabetic(10)
            def version = '1.19.0_078c802'
            def protocol = 'protocol'
            def serviceCapabilities = new ServiceContractsForm(
                ['protocol': new ServiceContractsForm.ContractForm( 'capabilities',  MediaType.TEXT_PLAIN_VALUE)], [:])
            mockMvc.perform(
                post(serviceNameVersionUrl(), serviceName, version)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(serviceCapabilities))
            ).andReturn().getResponse()
        when:
            def response = mockMvc.perform(
                get('/contracts/services/{serviceName}/versions/{version}/capabilities/{protocol}', serviceName, version, protocol)
            ).andReturn().getResponse()
        then:
            response.status == 200
            response.contentType.contains(MediaType.TEXT_PLAIN_VALUE)
            response.getContentAsString() == 'capabilities'
    }

    def 'cors configuration to allow using get capabilities url as input for web applications presenting swagger specs' () {
        when:
            def response = mockMvc.perform(options("/contracts/services/{serviceName}/versions/{version}/capabilities/{protocol}", "1", "2", "rest" )
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost")
            ).andReturn().getResponse()
        then:
            response.getStatus() == 200

    }

    private def createAService() {
        def version = '1.0'
        def serviceName = randomAlphabetic(10)
        mockMvc.perform(
            post(serviceNameVersionUrl(), serviceName, version)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(randomServiceContractFormWithExpectationsAndCapabilities()))
        ).andReturn().getResponse()
        [serviceName, version]
    }

    ServiceContractsForm randomServiceContractFormWithExpectationsAndCapabilities() {
        return new ServiceContractsForm(
            ['protocol': new ServiceContractsForm.ContractForm( 'capabilities',  MediaType.APPLICATION_JSON_VALUE)],
            ['some-other-provider': ['protocol': new ServiceContractsForm.ContractForm( 'expectations', MediaType.APPLICATION_JSON_VALUE)]]
        )
    }

    private String serviceNameVersionUrl() {
        '/contracts/services/{serviceName}/versions/{version}'
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        ServiceContractsRepository repository() {
            return new InMemoryServiceContractsRepository()
        }
    }
}

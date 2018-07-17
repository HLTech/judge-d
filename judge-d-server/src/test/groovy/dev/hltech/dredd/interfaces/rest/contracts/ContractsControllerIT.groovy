package dev.hltech.dredd.interfaces.rest.contracts

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(ContractsController.class)
@ActiveProfiles("test-integration")
class ContractsControllerIT extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private MockMvc mockMvc

    def 'return 404 when no contracts registered for given service'() {
        when: 'rest validatePacts url is hit'
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            def response = mockMvc.perform(
                    get('/contracts/'+ serviceName + '/' + version)
                        .contentType("application/json")
                ).andReturn().getResponse()
        then: 'controller returns 404'
            response.getStatus() == 404
    }

    def 'should return 200 and json when create a service contracts'() {
        when: 'rest validatePacts url is hit'
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            def response = mockMvc.perform(
                post('/contracts/'+ serviceName + '/' + version)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(randomServiceWithExpectationsAndCapabilities()))
            ).andReturn().getResponse()
        then: 'controller returns dto response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<ServiceContractsDto>(){})
    }


    def 'should return 200 and json when get previousl saved service contracts'() {
        given: 'rest validatePacts url is hit'
            def serviceName = randomAlphabetic(10)
            def version = '1.0'
            def serviceContractsForm = randomServiceWithExpectationsAndCapabilities()
            mockMvc.perform(
                post('/contracts/'+ serviceName + '/' + version)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(serviceContractsForm))
            ).andReturn().getResponse()
        when:
            def response = mockMvc.perform(
                 get('/contracts/'+ serviceName + '/' + version)
                    .contentType("application/json")
            ).andReturn().getResponse()
        then: 'controller returns dto response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<ServiceContractsDto>(){})
    }



    ServiceContractsForm randomServiceWithExpectationsAndCapabilities() {
        return new ServiceContractsForm(
            ['protocol' : 'capabilities'],
            ['some-other-provider': ['protocol':'expectations']]

        )
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        public ServiceContractsRepository repository(){
            return new InMemoryServiceContractsRepository();
        }

    }

}

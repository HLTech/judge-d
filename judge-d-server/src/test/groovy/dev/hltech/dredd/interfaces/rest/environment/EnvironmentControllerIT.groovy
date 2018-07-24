package dev.hltech.dredd.interfaces.rest.environment

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory

import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@WebMvcTest(EnvironmentController.class)
@ActiveProfiles("test-integration")
class EnvironmentControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    MockMvc mockMvc

    def "getServices test hits the URL and parses JSON output"() {
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get('/environments/SIT')
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ServiceDto>>(){}) != null
    }

    def 'update environment hits the url and receives 200'(){
        given:
            def environmentServices = newArrayList(randomServiceForm(), randomServiceForm())
        when:
            def response = mockMvc.perform(
                put('/environments/SIT')
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(environmentServices))
            ).andReturn().response
        then:
            response.status == 200
    }

    private ServiceForm randomServiceForm() {
        new ServiceForm()
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        EnvironmentRepository environmentRepository(){
            return new InMemoryEnvironmentRepository()
        }
    }

}

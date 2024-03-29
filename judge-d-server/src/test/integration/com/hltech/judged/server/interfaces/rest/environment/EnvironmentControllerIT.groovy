package com.hltech.judged.server.interfaces.rest.environment

import com.fasterxml.jackson.databind.ObjectMapper
import com.hltech.judged.server.config.BeanFactory
import com.hltech.judged.server.domain.JudgeDApplicationService
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
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
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@WebMvcTest(EnvironmentController.class)
@ActiveProfiles("test")
class EnvironmentControllerIT extends Specification {

    @Autowired
    InMemoryEnvironmentRepository environmentRepository

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    MockMvc mockMvc

    def cleanup() {
        environmentRepository.storage.clear()
    }

    def "get on not existing environment should end up with status 200 and an environment without any space"() {
        given:
            def environment = new Environment('SIT', new HashSet<Space>())
            environmentRepository.persist(environment)

        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get('/environments/SIT')
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            def responseBody = objectMapper.readValue(response.getContentAsString(), EnvironmentDto)
            responseBody.name == 'SIT'
            responseBody.spaces.size() == 0
    }

    def 'update environment hits the url and receives 200'() {
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

    def 'get on not existing environment should end up with status 404'() {
        when:
            def response = mockMvc.perform(
                get('/environments/unknown')
                    .accept("application/json")
            ).andReturn().getResponse()
        then:
            response.status == 404
    }

    private ServiceForm randomServiceForm() {
        new ServiceForm()
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        EnvironmentRepository environmentRepository() {
            return new InMemoryEnvironmentRepository()
        }

        @Bean
        JudgeDApplicationService judgeDApplicationService(EnvironmentRepository environmentRepository) {
            return new JudgeDApplicationService(environmentRepository, new InMemoryServiceContractsRepository(), [] as List)
        }
    }

}

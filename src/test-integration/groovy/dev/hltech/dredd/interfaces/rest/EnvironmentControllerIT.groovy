package dev.hltech.dredd.interfaces.rest

import au.com.dius.pact.model.RequestResponsePact
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.Fixtures
import dev.hltech.dredd.domain.environment.Environment
import dev.hltech.dredd.domain.environment.StaticEnvironment
import dev.hltech.dredd.interfaces.rest.environment.EnvironmentController
import dev.hltech.dredd.interfaces.rest.environment.ServiceDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.collect.Lists.newArrayList
import static com.google.common.io.ByteStreams.toByteArray
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(EnvironmentController.class)
@ActiveProfiles("test")
class EnvironmentControllerIT extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    MockMvc mockMvc

    def "getServices test hits the URL and parses JSON output"() {
        when: 'rest validatePacts url is hit'
            def response = mockMvc.perform(
                get('/environment/services')
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ServiceDto>>(){})
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration extends BeanFactory {

        @Bean
        Environment hlEnvironment() throws IOException {
            return Fixtures.environment()
        }

    }

}

package dev.hltech.dredd.interfaces.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.domain.Fixtures
import dev.hltech.dredd.domain.environment.Environment
import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient
import dev.hltech.dredd.interfaces.rest.environment.EnvironmentController
import dev.hltech.dredd.interfaces.rest.environment.ServiceDto
import feign.Feign
import io.fabric8.kubernetes.client.KubernetesClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

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
                get('/environment/services')
                    .accept("application/json")
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ServiceDto>>(){})
    }

    @TestConfiguration
    static class TestConfig extends BeanFactory {

        @Bean
        Environment hlEnvironment(KubernetesClient kubernetesClient,
                                  PactBrokerClient pactBrokerClient,
                                  ObjectMapper objectMapper,
                                  Feign feign) throws IOException {
            return Fixtures.environment()
        }

        @Bean
        EnvironmentRepository environmentRepository(){
            return new InMemoryEnvironmentRepository()
        }
    }

}

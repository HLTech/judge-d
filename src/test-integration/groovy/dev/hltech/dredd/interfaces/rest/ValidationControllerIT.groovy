package dev.hltech.dredd.interfaces.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import dev.hltech.dredd.config.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(ValidationController.class)
@ActiveProfiles("test")
class ValidationControllerIT extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    def "verifyPacts test hits the URL and parses JSON output"() {
        when: 'rest validatePacts url is hit'
        def response = mockMvc.perform(
                post('/verification/pacts')
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(newPactVerificationForm()))
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), AggregatedValidationResultDto.class)
    }

    def "verifySwagger test hits the URL and parses JSON output"() {
        when: 'rest validateSwagger url is hit'
            def response = mockMvc.perform(
                post('/verification/swagger')
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(newSwaggerVerificationForm()))
            ).andReturn().getResponse()
        then: 'controller returns validation response in json'
            response.getStatus() == 200
            response.getContentType().contains("application/json")
            objectMapper.readValue(response.getContentAsString(), AggregatedValidationResultDto.class)
    }

    private SwaggerValidationForm newSwaggerVerificationForm() {
        new SwaggerValidationForm()
    }

    private PactValidationForm newPactVerificationForm() {
        return new PactValidationForm(Lists.newArrayList((Object)objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))))
    }


    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration extends BeanFactory {

    }

}

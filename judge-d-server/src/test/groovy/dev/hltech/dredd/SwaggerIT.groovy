package dev.hltech.dredd

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.config.BeanFactory
import dev.hltech.dredd.config.SwaggerConfig
import dev.hltech.dredd.domain.JudgeD
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.interfaces.rest.contracts.ContractsController
import dev.hltech.dredd.interfaces.rest.contracts.ContractsMapper
import dev.hltech.dredd.interfaces.rest.environment.EnvironmentController
import dev.hltech.dredd.interfaces.rest.validation.ValidationController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultHandler
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest
@ContextConfiguration(classes = [SwaggerConfig, BeanFactory, ContractsController, EnvironmentController, ValidationController])
@ActiveProfiles("test")
class SwaggerIT extends Specification {

    @MockBean
    private ServiceContractsRepository serviceContractsRepository

    @MockBean
    private ContractsMapper contractsMapper

    @MockBean
    private EnvironmentRepository environmentRepository

    @MockBean
    private JudgeD judgeD

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper objectMapper

    def "should return 200 and swagger json file on swagger endpoint"() {
        when:
            def response = mockMvc.perform(get(swaggerPath()).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse()

        then:
            response.getStatus() == 200
            response.getContentType().contains("application/json")

        and:
            JsonNode responseJson = objectMapper.readTree(response.contentAsString)
            responseJson.findPath("tags").size() == 3

        and:
            def controllersNodes = responseJson.findPath("tags").findValues("name")
            controllersNodes.size() == 3
            controllersNodes.any { it.textValue() == "contracts-controller" }
            controllersNodes.any { it.textValue() == "environment-controller" }
            controllersNodes.any { it.textValue() == "validation-controller" }
    }

    def "should generate swagger json"() {
        given:
            ResultHandler resultHandler = { r ->
                def swaggerDir = new File('target/swagger')
                swaggerDir.mkdirs()
                def swaggerJsonFile = new File(swaggerDir, 'swagger.json')
                swaggerJsonFile.createNewFile()

                Files.write(Paths.get(swaggerJsonFile.absolutePath), r.getResponse().getContentAsString().getBytes('UTF-8'))
            }
        expect:
            mockMvc.perform(get(swaggerPath()).accept(MediaType.APPLICATION_JSON))
                .andDo(resultHandler)
    }

    private static String swaggerPath() {
        "/v2/api-docs"
    }
}

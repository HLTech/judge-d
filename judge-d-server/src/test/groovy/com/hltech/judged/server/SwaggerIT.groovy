package com.hltech.judged.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hltech.judged.server.domain.JudgeD
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.environment.EnvironmentRepository
import com.hltech.judged.server.interfaces.rest.contracts.ContractsMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultHandler
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
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
            responseJson.findPath("paths").size() == 12
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
        "/v3/api-docs"
    }
}

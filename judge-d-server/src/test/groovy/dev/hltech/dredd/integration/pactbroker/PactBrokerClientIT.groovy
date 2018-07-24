package dev.hltech.dredd.integration.pactbroker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.apache.tomcat.util.http.fileupload.util.Streams.asString
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureWireMock(port = 0, httpsPort = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-integration")
class PactBrokerClientIT extends Specification {

    @Autowired
    PactBrokerClient brokerClient

    def "should decode message as ObjectNode"() {
        given:
            stubFor(
                get("/pacts/provider/backend-provider/consumer/frontend/version/1.0")
                .willReturn(
                    aResponse()
                        .withBody(asString(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json")))
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
            )
        when:
            def pact = brokerClient.getPact("backend-provider", "frontend", "1.0")
        then:
            pact.getBody().findPath("provider").get("name").asText() == "backend-provider"
            pact.getBody().findPath("consumer").get("name").asText() == "frontend"
    }
}

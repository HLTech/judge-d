package dev.hltech.dredd.integration.kubernetes

import com.github.tomakehurst.wiremock.WireMockServer
import feign.Feign
import feign.Target
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static org.apache.tomcat.util.http.fileupload.util.Streams.asString
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureWireMock(port = 0, httpsPort = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-integration")
class PodClientIT extends Specification {

    @Autowired
    Feign feign

    @Autowired
    WireMockServer server

    def "should get info"() {
        given:
            PodClient podClient = feign.newInstance(new Target.HardCodedTarget<PodClient>(PodClient.class, "http://localhost:" + server.port()))
        and:
            stubFor(
                get("/info")
                    .willReturn(
                    aResponse()
                        .withBody(asString(getClass().getResourceAsStream("/info.json")))
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
            )

        when:
            def info = podClient.getInfo()

        then:
            info.get("build").get("name").asText() == "dde-instruction-quartz"
            info.get("build").get("version").asText() == "105-1e8fcf6"
    }

    def "should get swagger"() {
        given:
            PodClient podClient = feign.newInstance(new Target.HardCodedTarget<PodClient>(PodClient.class, "http://localhost:" + server.port()))

        and:
            stubFor(
                get("/dde-instruction-gateway/documentation/api-docs")
                    .willReturn(
                    aResponse()
                        .withBody("swagger")
                        .withStatus(200)
                )
            )

        when:
            def swagger = podClient.getSwagger(URI.create("dde-instruction-gateway"))

        then:
            swagger == "swagger"
    }

}

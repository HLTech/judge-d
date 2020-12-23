package com.hltech.judged.agent

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.ok
import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import static org.awaitility.Awaitility.await

@SpringBootTest
@ActiveProfiles(["test", "consul"])
class ConsulServiceLocatorFT extends Specification {

    @Value('${hltech.contracts.judge-d.updateServices.initialDelay}')
    Long updateServicesInitialDelay

    static WireMockRule wireMockRuleConsulServer
    static WireMockRule wireMockRuleJudgeDServer

    def setupSpec() {
        wireMockRuleConsulServer = new WireMockRule(8500)
        wireMockRuleConsulServer.start()
        wireMockRuleJudgeDServer = new WireMockRule(8080)
        wireMockRuleJudgeDServer.start()
    }

    def cleanupSpec() {
        wireMockRuleConsulServer.stop()
        wireMockRuleJudgeDServer.stop()
    }

    def "should send update services message with expected names and versions"() {
        given:
            wireMockRuleConsulServer.stubFor(
                get(urlPathMatching("/v1/catalog/services"))
                    .willReturn(okJson(
                    """
                      {
                        "test_app_1": ["version=78d588f9cc"],
                        "test_app_2": ["version=54d576f9dh"],
                        "test_app_3": ["version=98d500g9df"]
                      }
                    """
                    ))
            )

        and:
            stubServiceHealthResponse(1)
            stubServiceHealthResponse(2)
            stubServiceHealthResponse(3)

        and: "expected message to publish"
            def expectedRequestBody = """
                [
                    {
                        "name": "test_app_1",
                        "version": "78d588f9cc"
                    },
                    {
                        "name": "test_app_2",
                        "version": "54d576f9dh"
                    },
                    {
                        "name": "test_app_3",
                        "version": "98d500g9df"
                    }
                ]
            """
            wireMockRuleJudgeDServer.stubFor(
                put(urlPathMatching("/environments/test"))
                    .withRequestBody(equalToJson(expectedRequestBody, true, false))
                    .willReturn(ok())
            )

        expect: "update services message is sent with expected services names and versions"
            await().atMost(updateServicesInitialDelay + 5000, TimeUnit.MILLISECONDS).until({ wireMockRuleJudgeDServer.getServeEvents().requests.size() == 1 })
            wireMockRuleJudgeDServer.verify(1, putRequestedFor(urlPathMatching("/environments/test")).withRequestBody(equalToJson(expectedRequestBody, true, false)))
    }

    def stubServiceHealthResponse(int serviceTestId) {
        wireMockRuleConsulServer.stubFor(
            get(urlPathMatching("/v1/health/service/test_app_$serviceTestId"))
                .withQueryParam("passing", equalTo("true"))
                .willReturn(okJson(
                    """
                        [
                          {
                            "Node": {
                              "ID": "test_app_$serviceTestId",
                              "Node": "test_app_$serviceTestId"
                            }
                          }
                        ]
                    """
                ))
        )
    }


}

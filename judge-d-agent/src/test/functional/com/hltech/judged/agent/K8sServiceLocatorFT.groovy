package com.hltech.judged.agent

import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.fabric8.kubernetes.api.model.ContainerBuilder
import io.fabric8.kubernetes.api.model.NamespaceListBuilder
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodListBuilder
import io.fabric8.kubernetes.api.model.PodSpecBuilder
import io.fabric8.kubernetes.api.model.PodStatusBuilder
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.ok
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import static org.awaitility.Awaitility.await

@SpringBootTest
@ActiveProfiles(["test", "kubernetes"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class K8sServiceLocatorFT extends Specification {

    @Value('${hltech.contracts.judge-d.updateServices.initialDelay}')
    Long updateServicesInitialDelay

    @Shared
    KubernetesMockServer k8sServer

    static WireMockRule wireMockRule

    def setupSpec() {
        k8sServer = new KubernetesMockServer()
        k8sServer.init()
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, "https://${k8sServer.hostName}:$k8sServer.port/")
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true")
        wireMockRule = new WireMockRule(8080)
        wireMockRule.start()
    }

    def cleanupSpec() {
        wireMockRule.stop()
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    def "should send update services message with expected names and versions"() {
        given: "expected namespaces"
            k8sServer.expect().get().withPath("/api/v1/namespaces")
                .andReturn(200, new NamespaceListBuilder()
                    .addNewItem()
                    .withNewMetadata()
                    .withName("firstnamespace")
                    .endMetadata()
                    .and()
                    .addNewItem()
                    .withNewMetadata()
                    .withName("secondnamespace")
                    .endMetadata()
                    .and()
                    .build())
                .always()

        and: "expected pods"
            PodList expectedPodList = new PodListBuilder()
                .withItems(
                    buildPod("firstnamespace", 1, "78d588f9cc"),
                    buildPod("firstnamespace", 2, "54d576f9dh"),
                    buildPod("secondnamespace", 3, "98d500g9df")
                ).build()

            k8sServer.expect().get().withPath("/api/v1/pods?labelSelector=app")
                .andReturn(HttpURLConnection.HTTP_OK, expectedPodList)
                .always()

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

            wireMockRule.stubFor(
                put(urlPathMatching("/environments/test"))
                    .withRequestBody(equalToJson(expectedRequestBody, true, false))
                    .willReturn(ok())
            )


        expect: "update services message is sent with expected services names and versions"
            await().atMost(updateServicesInitialDelay + 5000, TimeUnit.MILLISECONDS).until({ wireMockRule.getServeEvents().requests.size() == 1 })
            wireMockRule.verify(1, putRequestedFor(urlPathMatching("/environments/test")).withRequestBody(equalToJson(expectedRequestBody, true, false)))
    }

    def buildPod(String namespace, int index, String version) {
        new PodBuilder().withNewMetadata()
            .withName("pod$index")
            .withNamespace(namespace)
            .endMetadata()
            .withSpec(
                new PodSpecBuilder().withContainers(
                    new ContainerBuilder().withImage("test_app_$index:$version").build()
                ).build()
            )
            .withStatus(new PodStatusBuilder().withPhase("running").build())
            .build()
    }
}

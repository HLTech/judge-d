package com.hltech.contracts.judged.agent.hltech

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.ImmutableMap
import feign.Feign
import feign.Target
import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerPort
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import org.assertj.core.util.Lists
import spock.lang.Specification
import org.springframework.http.ResponseEntity

import static com.google.common.collect.Lists.newArrayList

class K8sLabelBasedServiceLocatorUT extends Specification {

    private Feign feign
    private HLTechServiceLocator serviceLocator
    private KubernetesClient kubernetesClient

    def setup(){
        feign = Mock()
        kubernetesClient = Mock()
        serviceLocator = new HLTechServiceLocator(kubernetesClient, feign)
    }

    def 'should correctly locate a service fulfilling all conditions'(){
        given: "data of pods"
            def name1 = 'test-service-1'
            def version1 = '1.0.0'

        and: "pods are present in kubernetes environment"
            def mixedOperationMock = Mock(MixedOperation)
            kubernetesClient.pods() >> mixedOperationMock
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock

            def podListMock = Mock(PodList)
            mixedOperationMock.list() >> podListMock

            podListMock.getItems() >> newArrayList(createPodFulfillingAllConditions(name1))

        and: "version of pods is accessible using feign"
            def podClient = Mock(PodClient)
            def response = Mock(ResponseEntity)
            def body = Mock(JsonNode)
            def build = Mock(JsonNode)
            def version = Mock(JsonNode)
            feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + 123)) >> podClient
            podClient.getInfo() >> response
            response.getBody() >> body
            body.get('build') >> build
            build.get('version') >> version
            version.asText() >> version1

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services having required labels and exposing version are found"
            services.size() == 1
            services.find {it.name == name1} .version == version1
    }

    def 'should correctly locate a service fulfilling all conditions except port availability'(){
        given: "data of pods"
            def name1 = 'test-service-1'
            def version1 = '1.0.0'

        and: "pods are present in kubernetes environment"
            def mixedOperationMock = Mock(MixedOperation)
            kubernetesClient.pods() >> mixedOperationMock
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock

            def podListMock = Mock(PodList)
            mixedOperationMock.list() >> podListMock

            podListMock.getItems() >> newArrayList(createPodFulfillingAllConditionsExceptPortAvailability(name1))

        and: "version of pods is accessible using feign"
            def podClient = Mock(PodClient)
            def response = Mock(ResponseEntity)
            def body = Mock(JsonNode)
            def build = Mock(JsonNode)
            def version = Mock(JsonNode)
            feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + HLTechServiceLocator.DEFAULT_CONTAINER_VERSION_PORT)) >> podClient
            podClient.getInfo() >> response
            response.getBody() >> body
            body.get('build') >> build
            build.get('version') >> version
            version.asText() >> version1

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services having required labels and exposing version are found"
            services.size() == 1
            services.find {it.name == name1} .version == version1
    }

    def 'should not locate a service when a label is missing'(){
        given: "data of pods"
            def name1 = 'test-service-1'
            def version1 = '1.0.0'

        and: "pods are present in kubernetes environment"
            def mixedOperationMock = Mock(MixedOperation)
            kubernetesClient.pods() >> mixedOperationMock
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock

            def podListMock = Mock(PodList)
            mixedOperationMock.list() >> podListMock

            podListMock.getItems() >> newArrayList(new Pod())

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services without required labels are not found"
            services.size() == 0
    }

    def createPodFulfillingAllConditions(String serviceName) {
        def objectMetadata = new ObjectMeta(labels: ImmutableMap.of("app", serviceName))
        def containerPort = new ContainerPort(containerPort: 123, name: 'monitoring')
        def container = new Container(name: serviceName, ports: newArrayList(containerPort))
        def podSpec = new PodSpec(containers: Lists.newArrayList(container))
        def podStatus = new PodStatus(podIP: '127.0.0.1')

        return new Pod(metadata: objectMetadata, spec: podSpec, status: podStatus)
    }

    def createPodFulfillingAllConditionsExceptPortAvailability(String serviceName) {
        def objectMetadata = new ObjectMeta(labels: ImmutableMap.of("app", serviceName))
        def podSpec = new PodSpec(containers: new ArrayList<Container>())
        def podStatus = new PodStatus(podIP: '127.0.0.1')

        return new Pod(metadata: objectMetadata, spec: podSpec, status: podStatus)
    }
}

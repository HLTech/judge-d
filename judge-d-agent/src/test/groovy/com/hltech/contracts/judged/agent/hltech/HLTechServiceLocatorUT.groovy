package com.hltech.contracts.judged.agent.hltech

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
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
import spock.lang.Subject

import static com.google.common.collect.Lists.newArrayList

class HLTechServiceLocatorUT extends Specification {

    private Feign feign
    private KubernetesClient kubernetesClient

    @Subject
    private HLTechServiceLocator serviceLocator

    def setup(){
        feign = Mock()
        kubernetesClient = Mock()
        serviceLocator = new HLTechServiceLocator(kubernetesClient, feign)
    }

    def 'should correctly locate a service fulfilling all conditions'(){
        given: "data of pods"
            def name = 'test-service-1'
            def version = '1.0.0'

        and: "pods are present in kubernetes environment"
            def podListMock = Mock(PodList) { getItems() >> newArrayList(createPodFulfillingAllConditions(name)) }
            def mixedOperationMock = Mock(MixedOperation) { list() >> podListMock }
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock
            kubernetesClient.pods() >> mixedOperationMock

        and: "version of pods is accessible using feign"
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode()
            ObjectNode anotherObjectNode = JsonNodeFactory.instance.objectNode()
            anotherObjectNode.put('version', version)
            objectNode.set('build', anotherObjectNode)

            def response = Mock(ResponseEntity) { getBody() >> objectNode }
            def podClient = Mock(PodClient) { getInfo() >> response }

            feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + 123)) >> podClient

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services having required labels and exposing version are found"
            services.size() == 1
            services.any {it.name == name && it.version == version}
    }

    def 'should correctly locate a service fulfilling all conditions except port availability'(){
        given: "data of pods"
            def name = 'test-service-1'
            def version = '1.0.0'

        and: "pods are present in kubernetes environment"
            def podListMock = Mock(PodList) { getItems() >> newArrayList(createPodFulfillingAllConditionsExceptPortAvailability(name)) }
            def mixedOperationMock = Mock(MixedOperation) { list() >> podListMock }
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock
            kubernetesClient.pods() >> mixedOperationMock

        and: "version of pods is accessible using feign"
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode()
            ObjectNode anotherObjectNode = JsonNodeFactory.instance.objectNode()
            anotherObjectNode.put('version', version)
            objectNode.set('build', anotherObjectNode)

            def response = Mock(ResponseEntity) { getBody() >> objectNode }
            def podClient = Mock(PodClient) { getInfo() >> response }

            feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + HLTechServiceLocator.DEFAULT_CONTAINER_VERSION_PORT)) >> podClient

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services having required labels and exposing version are found"
            services.size() == 1
            services.find {it.name == name && it.version == version}
    }

    def 'should not locate a service when a label is missing'(){
        given: "pods are present in kubernetes environment"
            def podListMock = Mock(PodList) { getItems() >> newArrayList(new Pod()) }
            def mixedOperationMock = Mock(MixedOperation) { list() >> podListMock }
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock

            kubernetesClient.pods() >> mixedOperationMock

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

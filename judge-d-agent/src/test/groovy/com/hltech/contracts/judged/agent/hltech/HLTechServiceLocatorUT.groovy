package com.hltech.contracts.judged.agent.hltech

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
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
import spock.lang.Specification
import org.springframework.http.ResponseEntity
import spock.lang.Subject

class HLTechServiceLocatorUT extends Specification {

    private def feign = Mock(Feign)
    private def kubernetesClient = Mock(KubernetesClient)

    @Subject
    private HLTechServiceLocator serviceLocator = new HLTechServiceLocator(kubernetesClient, feign)

    def 'should find all correctly configured services'(){
        given: "data of pods"
            def servicesVersion = '1.0.0'
            def service1Name = 'test-service-1'
            def service1Port = 123
            def service2Name = 'test-service-2'
            def service2Port = HLTechServiceLocator.DEFAULT_CONTAINER_VERSION_PORT
            def service4Name = 'test-service-4'

        and: "pods are present in kubernetes environment"
            def podListMock = Mock(PodList) { getItems() >> [createPod([new Container(name: service1Name, ports: [new ContainerPort(containerPort: 123, name: 'monitoring')])], ["app": service1Name]),
                                                             createPod(new ArrayList<Container>(), ["app": service2Name]),
                                                             createPod(new ArrayList<Container>(), new HashMap<String, String>()),
                                                             createPod(new ArrayList<Container>(), ["app": service4Name, 'exclude-from-judged-jurisdiction': 'true'])] }
            def mixedOperationMock = Mock(MixedOperation) { list() >> podListMock }
            mixedOperationMock.inAnyNamespace() >> mixedOperationMock
            kubernetesClient.pods() >> mixedOperationMock

        and: "version of pods is accessible using feign"
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode()
            ObjectNode anotherObjectNode = JsonNodeFactory.instance.objectNode()
            anotherObjectNode.put('version', servicesVersion)
            objectNode.set('build', anotherObjectNode)

            def response = Mock(ResponseEntity) { getBody() >> objectNode }
            def podClient = Mock(PodClient) { getInfo() >> response }

            1 * feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + service1Port)) >> podClient
            1 * feign.newInstance(new Target.HardCodedTarget(PodClient.class, "http://" + '127.0.0.1' + ":" + service2Port)) >> podClient

        when: "agent tries to find services"
            def services = serviceLocator.locateServices()

        then: "available services having required labels and exposing version are found"
            services.size() == 2
            services.find {it.name == service1Name} .version == "1.0.0"
            services.find {it.name == service2Name} .version == "1.0.0"
    }

    def createPod(List<Container> containers, Map<String, String> labels) {
        def objectMetadata = new ObjectMeta(labels: labels)
        def podSpec = new PodSpec(containers: containers)
        def podStatus = new PodStatus(podIP: '127.0.0.1')

        return new Pod(metadata: objectMetadata, spec: podSpec, status: podStatus)
    }
}

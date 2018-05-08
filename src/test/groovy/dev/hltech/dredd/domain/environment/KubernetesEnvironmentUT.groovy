package dev.hltech.dredd.domain.environment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient
import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerPort
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

import static org.apache.tomcat.util.http.fileupload.util.Streams.asString

class KubernetesEnvironmentUT extends Specification {

    def client = Mock(KubernetesClient)
    def restTemplate = Mock(RestTemplate)
    def pactBrokerClient = Mock(PactBrokerClient)
    def objectMapper = new ObjectMapper()

    @Subject
    KubernetesEnvironment environment = new KubernetesEnvironment(client, restTemplate, pactBrokerClient, objectMapper)

    def 'should find 0 services when 0 pods found in kubernetes' () {
        given:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    list() >> Mock(PodList) {
                        getItems() >> Lists.newArrayList()
                    }
                }
            }

        when:
            Collection<Service> services = environment.getAllServices()

        then:
            services.size() == 0
    }

    def 'should not find defined service when 0 pods found in kubernetes' () {
        given:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    withLabel(*_) >> Mock(MixedOperation) {
                        list() >> Mock(PodList) {
                            getItems() >> Lists.newArrayList()
                        }
                    }
                }
            }

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 0
    }

    def 'should not find a service when an exception is thrown during service recollection' () {
        given:
            def containerPort = Mock(ContainerPort) {
                getContainerPort() >> 1
            }

            def container = Mock(Container) {
                getName() >> "a name"
                getPorts() >> Lists.newArrayList(containerPort)
            }

            def pod = Mock(Pod) {
                getMetadata() >> Mock(ObjectMeta) {
                    getLabels() >> Maps.newHashMap().put("app", "a name")
                }
                getStatus() >> Mock(PodStatus) {
                    getPodIP() >> "IP"
                }
                getSpec() >> Mock(PodSpec) {
                    getContainers() >> Lists.newArrayList(container)
                }
            }

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    list() >> Mock(PodList) {
                        getItems() >> Lists.newArrayList(pod)
                    }
                }
            }

        and:
        restTemplate.getForObject(*_) >> {
            throw new RuntimeException()
        }

        when:
            Collection<Service> services = environment.getAllServices()

        then:
            services.size() == 0
    }

    def 'should not find defined service when an exception is thrown during service recollection' () {
        given:
            def containerPort = Mock(ContainerPort) {
                getContainerPort() >> 1
            }

            def container = Mock(Container) {
                getName() >> "a name"
                getPorts() >> Lists.newArrayList(containerPort)
            }

            def pod = Mock(Pod) {
                getMetadata() >> Mock(ObjectMeta) {
                    getLabels() >> Maps.newHashMap().put("app", "a name")
                }
                getStatus() >> Mock(PodStatus) {
                    getPodIP() >> "IP"
                }
                getSpec() >> Mock(PodSpec) {
                    getContainers() >> Lists.newArrayList(container)
                }
            }

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    withLabel(_, _) >> Mock(MixedOperation) {
                        list() >> Mock(PodList) {
                            getItems() >> Lists.newArrayList(pod)
                        }
                    }
                }
            }

        and:
            restTemplate.getForObject(*_) >> {
                throw new RuntimeException()
            }

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 0
    }

    def 'should return all available services' () {
        given:
            def containerPort = Mock(ContainerPort) {
                getContainerPort() >> 1
            }

            def container = Mock(Container) {
                getName() >> "a name"
                getPorts() >> Lists.newArrayList(containerPort)
            }

            Map<String, String> labelsMap = new HashMap<>()
            labelsMap.put("app", "a name")
            def pod = Mock(Pod) {
                getMetadata() >> Mock(ObjectMeta) {
                    getLabels() >> labelsMap
                }
                getStatus() >> Mock(PodStatus) {
                    getPodIP() >> "IP"
                }
                getSpec() >> Mock(PodSpec) {
                    getContainers() >> Lists.newArrayList(container)
                }
            }

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    list() >> Mock(PodList) {
                        getItems() >> Lists.newArrayList(pod)
                    }
                }
            }

        and:
            ObjectMapper mapper = new ObjectMapper()
            JsonNode version = mapper.createObjectNode()
            version.put("version", "a version")
            JsonNode build = mapper.createObjectNode()
            build.set("build", version)
            restTemplate.getForObject(_, JsonNode.class) >> build

        and:
            def swagger = "a swagger"
            restTemplate.getForObject(_, String.class) >> swagger

        and:
            def pactString = asString(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> pact

        when:
            Collection<Service> services = environment.getAllServices()

        then:
            services.size() == 1
            services[0].getName() == "a name"
            services[0].getVersion() == "a version"
            services[0].asProvider().isPresent()
            services[0].asProvider().get().getSwagger() == swagger
            services[0].asConsumer().get().getPact("dde-instruction-gateway") != null
    }

    def 'should return the requested service' () {
        given:
            def containerPort = Mock(ContainerPort) {
                getContainerPort() >> 1
            }

            def container = Mock(Container) {
                getName() >> "a name"
                getPorts() >> Lists.newArrayList(containerPort)
            }

            Map<String, String> labelsMap = new HashMap<>()
            labelsMap.put("app", "a name")
            def pod = Mock(Pod) {
                getMetadata() >> Mock(ObjectMeta) {
                    getLabels() >> labelsMap
                }
                getStatus() >> Mock(PodStatus) {
                    getPodIP() >> "IP"
                }
                getSpec() >> Mock(PodSpec) {
                    getContainers() >> Lists.newArrayList(container)
                }
            }

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    withLabel(_, _) >> Mock(MixedOperation) {
                        list() >> Mock(PodList) {
                            getItems() >> Lists.newArrayList(pod)
                        }
                    }
                }
            }

        and:
            ObjectMapper mapper = new ObjectMapper()
            JsonNode version = mapper.createObjectNode()
            version.put("version", "a version")
            JsonNode build = mapper.createObjectNode()
            build.set("build", version)
            restTemplate.getForObject(_, JsonNode.class) >> build

        and:
            def swagger = "a swagger"
            restTemplate.getForObject(_, String.class) >> swagger

        and:
            def pactString = asString(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> pact

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 1
            services[0].getName() == "a name"
            services[0].getVersion() == "a version"
            services[0].asProvider().isPresent()
            services[0].asProvider().get().getSwagger() == swagger
            services[0].asConsumer().get().getPact("dde-instruction-gateway") != null
    }

}

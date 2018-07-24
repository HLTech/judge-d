package dev.hltech.dredd.domain.environment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.hltech.dredd.integration.kubernetes.PodClient
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient
import feign.Feign
import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerPort
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import unfiltered.response.Ok

import static org.apache.tomcat.util.http.fileupload.util.Streams.asString

class KubernetesEnvironmentUT extends Specification {

    def client = Mock(KubernetesClient)
    def pactBrokerClient = Mock(PactBrokerClient)
    def objectMapper = new ObjectMapper()
    def podClient = Mock(PodClient)
    def feign = Mock(Feign) {
        newInstance(*_) >> podClient
    }

    @Subject
    KubernetesEnvironment environment = new KubernetesEnvironment(client, pactBrokerClient, objectMapper, feign)

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
            def pod = mockPod()

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    list() >> Mock(PodList) {
                        getItems() >> Lists.newArrayList(pod)
                    }
                }
            }

        and:
            podClient.getInfo() >> {
            throw new RuntimeException()
        }

        when:
            Collection<Service> services = environment.getAllServices()

        then:
            services.size() == 0
    }

    def 'should not find defined service when an exception is thrown during service recollection' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> {
                throw new RuntimeException()
            }

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 0
    }

    def 'should return all available services' () {
        given:
            def pod = mockPod()

        and:
            client.pods() >> Mock(MixedOperation) {
                inAnyNamespace() >> Mock(MixedOperation) {
                    list() >> Mock(PodList) {
                        getItems() >> Lists.newArrayList(pod)
                    }
                }
            }

        and:
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(swagger, HttpStatus.OK)

        and:
            def pactString = asString(getClass().getResourceAsStream("/backend-provider-swagger.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, HttpStatus.OK)

        when:
            Collection<Service> services = environment.getAllServices()

        then:
            services.size() == 1
            services[0].getName() == "name"
            services[0].getVersion() == "a version"
            services[0].asProvider().getSwagger().get() == swagger
            services[0].asConsumer().getPact("backend-provider").get() != null
    }

    def 'should return the requested service' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(swagger, HttpStatus.OK)

        and:
            def pactString = asString(getClass().getResourceAsStream("/backend-provider-swagger.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, HttpStatus.OK)

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 1
            services[0].getName() == "name"
            services[0].getVersion() == "a version"
            services[0].asProvider().getSwagger().get() == swagger
            services[0].asConsumer().getPact("backend-provider").get() != null
    }

    def 'a service should not be consumer if HTTP status code from pact broker client is 404' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(swagger, HttpStatus.OK)

        and:
            def pactString = asString(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, HttpStatus.NOT_FOUND)

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 1
            services[0].getName() == "name"
            services[0].getVersion() == "a version"
            services[0].asProvider().getSwagger().get() == swagger
            !services[0].asConsumer().getPact("backend-provider").isPresent()
    }

    def 'a service should not be provider if HTTP status code after swagger resolution is 404' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(swagger, HttpStatus.NOT_FOUND)

        and:
            def pactString = asString(getClass().getResourceAsStream("/backend-provider-swagger.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, HttpStatus.OK)

        when:
            Collection<Service> services = environment.findServices("a service")

        then:
            services.size() == 1
            services[0].getName() == "name"
            services[0].getVersion() == "a version"
            !services[0].asProvider().getSwagger().isPresent()
            services[0].asConsumer().getPact("backend-provider").get() != null
    }

    @Unroll
    def 'exception should be thrown if HTTP status code from pact broker client is #httpStatus' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(swagger, HttpStatus.OK)

        and:
            def pactString = asString(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, httpStatus)

        when:
            Collection<Service> services = environment.findServices("a service")
            Service service = ++services.iterator()
            service.asConsumer().getPact("a service")

        then:
            thrown KubernetesEnvironmentException

        where:
            httpStatus << [HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.GATEWAY_TIMEOUT]
    }

    @Unroll
    def 'exception should be thrown if HTTP status code after swagger resolution is #httpStatus' () {
        given:
            def pod = mockPod()

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
            podClient.getInfo() >> new ResponseEntity<>(createInfo(), HttpStatus.OK)

        and:
            def swagger = "a swagger"
            podClient.getSwagger(*_) >> new ResponseEntity<>(null, httpStatus)

        and:
            def pactString = asString(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
            def pact = objectMapper.readValue(pactString, ObjectNode.class)
            pactBrokerClient.getPact(*_) >> new ResponseEntity<>(pact, HttpStatus.OK)

        when:
            Collection<Service> services = environment.findServices("a service")
            Service service = ++services.iterator()
            service.asProvider().getSwagger()

        then:
            thrown KubernetesEnvironmentException

        where:
            httpStatus << [HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.GATEWAY_TIMEOUT]
    }

    Pod mockPod() {
        def containerPort = Mock(ContainerPort) {
            getContainerPort() >> 1
        }

        Container container = Mock(Container) {
            getName() >> "name"
            getPorts() >> Lists.newArrayList(containerPort)
        }

        Map<String, String> labelsMap = new HashMap<>()
        labelsMap.put("app", "name")

        Mock(Pod) {
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
    }

    JsonNode createInfo() {
        ObjectMapper mapper = new ObjectMapper()
        JsonNode version = mapper.createObjectNode()
        version.put("version", "a version")
        JsonNode build = mapper.createObjectNode()
        build.set("build", version)
    }

}

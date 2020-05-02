package com.hltech.contracts.judged.agent.consul

import com.ecwid.consul.transport.HttpResponse
import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.Response
import com.ecwid.consul.v1.agent.model.Service
import spock.lang.Specification
import spock.lang.Subject

class ConsulTagBasedServiceLocatorUT extends Specification {

    def consulClient = Mock(ConsulClient)

    @Subject
    def consulTagBasedServiceLocator = new ConsulTagBasedServiceLocator(consulClient)

    def 'Should find all services registered in consul having tag \'version=someVerion\' '() {
        given: 'Services having version tag'
            def servicesWithVersionTag = []
            3.times { servicesWithVersionTag.add(randomServiceWithVersionTag) }

        and: 'Services without version tag'
            def servicesWithoutVersionTag = []
            3.times { servicesWithoutVersionTag.add(getRandomServiceWithVersionTag(randomString, [])) }

        and: 'Mock consulClient to return services having version tag'
            def allServices = servicesWithVersionTag + servicesWithoutVersionTag
            consulClient.getAgentServices() >> new Response(allServices.collectEntries { [(it.service): it] }, httpOkResponse)

        when:
            def services = consulTagBasedServiceLocator.locateServices()

        then:
            services.size() == 3
            services.forEach { service ->
                assert servicesWithVersionTag.find { it.service == service.name && it.tags.first().split("version=")[1] == service.version }
            }
    }

    def 'When more than one services with the same name found should return the first randomly selected'() {
        given: 'Services having version tag'
            def servicesWithVersionTag = []
            3.times { servicesWithVersionTag.add(getRandomServiceWithVersionTag('service')) }

        and: 'Mock consulClient to return services having version tag'
            consulClient.getAgentServices() >> new Response(servicesWithVersionTag.collectEntries { [(it.service): it] }, httpOkResponse)

        when:
            def services = consulTagBasedServiceLocator.locateServices()

        then:
            services.size() == 1
    }

    private static getRandomString() {
        UUID.randomUUID().toString()
    }

    private static Service getRandomServiceWithVersionTag(String serviceName = randomString,
                                                          List<String> tags = ["version=${randomString}" as String]) {
        new Service(id: "${serviceName}-${randomString}" as String, service: serviceName, tags: tags)
    }

    private static HttpResponse getHttpOkResponse() {
        new HttpResponse(200, 'OK', 'content', 1L, false, 1L)
    }
}

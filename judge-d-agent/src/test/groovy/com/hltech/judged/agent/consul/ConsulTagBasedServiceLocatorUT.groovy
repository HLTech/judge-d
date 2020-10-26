package com.hltech.judged.agent.consul

import com.ecwid.consul.transport.HttpResponse
import com.ecwid.consul.v1.Response
import com.ecwid.consul.v1.catalog.CatalogClient
import com.ecwid.consul.v1.health.HealthClient
import com.ecwid.consul.v1.health.HealthServicesRequest
import com.ecwid.consul.v1.health.model.HealthService
import spock.lang.Specification
import spock.lang.Subject

class ConsulTagBasedServiceLocatorUT extends Specification {

    def catalogConsulClientMock = Mock(CatalogClient)
    def healthConsulClientMock = Mock(HealthClient)

    @Subject
    def consulTagBasedServiceLocator = new ConsulTagBasedServiceLocator(catalogConsulClientMock, healthConsulClientMock)

    def 'Should find all services registered in consul having tag \'version=someVersion\' and being healthy'() {
        given: 'Map with service names and tag list including version tag'
            def servicesWithVersionTag = [:] as Map
            3.times { servicesWithVersionTag.put((randomString), ["version=${randomString}" as String]) }

        and: 'Map with service names and tag list without version tag'
            def servicesWithoutVersionTag = [:]
            3.times { servicesWithoutVersionTag.put((randomString), []) }

        and: 'Mock catalogConsulClient to return services having version tag'
            def allServices = servicesWithVersionTag + servicesWithoutVersionTag
            catalogConsulClientMock.getCatalogServices(_) >> new Response(allServices, httpOkResponse)

        and: 'Mock healthConsulClientMock to return health services for single service having version tag'
            def healthyServiceWithVersionTagName = servicesWithVersionTag.keySet().first() as String
                healthConsulClientMock.getHealthServices(healthyServiceWithVersionTagName, _ as HealthServicesRequest) >>
                    new Response([getHealthService(healthyServiceWithVersionTagName)], httpOkResponse)

        and: 'Mock healthConsulClientMock to return empty list for rest of services with version'
            healthConsulClientMock.getHealthServices(servicesWithVersionTag.keySet()[1], _ as HealthServicesRequest) >> new Response([], httpOkResponse)
            healthConsulClientMock.getHealthServices(servicesWithVersionTag.keySet()[2], _ as HealthServicesRequest) >> new Response([], httpOkResponse)

        when:
            def services = consulTagBasedServiceLocator.locateServices()

        then:
            services.size() == 1
            with(services.first()) {
                name == healthyServiceWithVersionTagName
                version == servicesWithVersionTag.get(healthyServiceWithVersionTagName).first().split('=')[1]
            }
    }

    private static getRandomString() {
        UUID.randomUUID().toString()
    }

    private static HealthService getHealthService(String serviceName) {
        def serviceWithServiceName = new HealthService.Service()
        serviceWithServiceName.service = serviceName
        def healthService = new HealthService()
        healthService.service = serviceWithServiceName
        healthService
    }

    private static HttpResponse getHttpOkResponse() {
        new HttpResponse(200, 'OK', 'content', 1L, false, 1L)
    }
}

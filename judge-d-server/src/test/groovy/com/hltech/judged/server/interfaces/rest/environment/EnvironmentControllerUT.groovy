package com.hltech.judged.server.interfaces.rest.environment

import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.environment.Space
import spock.lang.Specification

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

class EnvironmentControllerUT extends Specification {

    def environmentRepository = new InMemoryEnvironmentRepository()
    def environmentController = new EnvironmentController(environmentRepository)

    def 'should return names of created environments'() {
        given:
            def environment = new Environment(
                randomAlphabetic(10),
                [] as Set
            )
            environmentRepository.persist(environment)
        when:
            def names = environmentController.environmentNames
        then:
            names == [environment.name] as Set
    }

    def 'should return empty list of services when get services giveen no such environment was saved before'() {
        when:
            def services = environmentController.getEnvironment("some-environment")
        then:
            services.isEmpty()
    }

    def 'should return list of services from and environment given it was saved before'() {
        def service = new ServiceId("service", "version")
        given:
            def environment = new Environment(
                randomAlphabetic(10),
                [new Space('def', [service] as Set)] as Set
            )
            environmentRepository.persist(environment)
        when:
            def services = environmentController.getEnvironment(environment.name)
        then:
            services == [new ServiceDto(service.name, service.version)] as List
    }

    def 'should return two services given agents from two different spaces saved before'(){
        given:
            def sv1 = new ServiceForm("service1", "version1")
            def sv2 = new ServiceForm("service2", "version2")

            environmentController.overwriteEnvironment("env", null, [sv1] as Set)
            environmentController.overwriteEnvironment("env", "space", [sv2] as Set)
        when:
            def environment = environmentController.getEnvironment("env")
        then:
            environment.size() == 2

    }

    def 'should override dfault space only given additional non-empty space exists and both are overwriten'(){
        given:
            def sv1 = new ServiceForm("service1", "version1")
            def sv2 = new ServiceForm("service2", "version2")
            def sv3 = new ServiceForm("service3", "version3")

            environmentController.overwriteEnvironment("env", null, [sv1] as Set)
            environmentController.overwriteEnvironment("env", null, [sv2] as Set)
            environmentController.overwriteEnvironment("env", "space", [sv3] as Set)
        when:
            def environment = environmentController.getEnvironment("env")
        then:
            environment.size() == 2
            environment.contains(new ServiceDto("service2", "version2"))
            environment.contains(new ServiceDto("service3", "version3"))

    }

}

package com.hltech.judged.server.interfaces.rest.environment


import spock.lang.Specification

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

class EnvironmentControllerUT extends Specification {

    def environmentRepository = new com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository()
    def environmentController = new EnvironmentController(environmentRepository)

    def 'should return names of created environments'() {
        given:
            def environment = new com.hltech.judged.server.domain.environment.EnvironmentAggregate(
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

    def 'should return list of services from and evironment given it was saved before'() {
        def serviceVersion = new com.hltech.judged.server.domain.ServiceVersion("service", "version")
        given:
            def environment = new com.hltech.judged.server.domain.environment.EnvironmentAggregate(
                randomAlphabetic(10),
                [serviceVersion] as Set
            )
            environmentRepository.persist(environment)
        when:
            def services = environmentController.getEnvironment(environment.name)
        then:
            services == [new ServiceDto(serviceVersion.name, serviceVersion.version)] as List
    }

    def 'should return two services given agents from two different spaces saved before'(){
        given:
            def sv1 = new ServiceForm("service1", "version1")
            def sv2 = new ServiceForm("service2", "version2")

            environmentController.overwriteEnvironment("env", null, [sv1] as Set);
            environmentController.overwriteEnvironment("env", "space", [sv2] as Set);
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

            environmentController.overwriteEnvironment("env", null, [sv1] as Set);
            environmentController.overwriteEnvironment("env", null, [sv2] as Set);
            environmentController.overwriteEnvironment("env", "space", [sv3] as Set);
        when:
            def environment = environmentController.getEnvironment("env")
        then:
            environment.size() == 2
            environment.contains(new ServiceDto("service2", "version2"))
            environment.contains(new ServiceDto("service3", "version3"))

    }

}

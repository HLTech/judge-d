package dev.hltech.dredd.interfaces.rest.environment


import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import spock.lang.Specification

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic

class EnvironmentControllerUT extends Specification {

    def environmentRepository = new InMemoryEnvironmentRepository();
    def environmentController = new EnvironmentController(environmentRepository)

    def 'should return names of created environments'(){
        given:
            def environment = new EnvironmentAggregate(
                randomAlphabetic(10),
                [] as Set
            )
            environmentRepository.persist(environment)
        when:
            def names = environmentController.environmentNames
        then:
            names == [environment.name] as Set
    }

    def 'should return empty list of services when get services giveen no such environment was saved before'(){
        when:
            def services = environmentController.getEnvironment("some-environment")
        then:
            services.isEmpty()
    }

    def 'should return list of services from and evironment given it was saved before'(){
        def serviceVersion = new EnvironmentAggregate.ServiceVersion("service", "version")
        given:
            def environment = new EnvironmentAggregate(
                randomAlphabetic(10),
                [serviceVersion] as Set
            )
            environmentRepository.persist(environment)
        when:
            def services = environmentController.getEnvironment(environment.name)
        then:
            services == [new ServiceDto(serviceVersion.name, serviceVersion.version)] as List
    }

}

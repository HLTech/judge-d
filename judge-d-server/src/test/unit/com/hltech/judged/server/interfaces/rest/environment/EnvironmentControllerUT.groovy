package com.hltech.judged.server.interfaces.rest.environment

import com.hltech.judged.server.domain.JudgeDApplicationService
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.environment.Space
import spock.lang.Specification

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

class EnvironmentControllerUT extends Specification {

    def judgeD = Mock(JudgeDApplicationService)
    def environmentRepository = new InMemoryEnvironmentRepository()
    def environmentController = new EnvironmentController(judgeD, environmentRepository)

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

    def 'should return empty list of services when no such environment was saved before'() {
        when:
            def services = environmentController.getEnvironment("some-environment").getBody()
        then:
            !services
    }

    def 'should return list of services from and environment given it was saved before'() {
        def service = new ServiceId("service", "version")
        given:
            def environment = new Environment(
                'abc',
                [new Space('def', [service] as Set)] as Set
            )
            environmentRepository.persist(environment)
        when:
            def receivedEnvironment = environmentController.getEnvironment(environment.name).getBody()

        then:
            receivedEnvironment ==
                new EnvironmentDto(
                    'abc',
                    [new EnvironmentDto.SpaceDto(
                        'def',
                        [new EnvironmentDto.SpaceDto.ServiceDto(service.name, service.version)] as Set
                    )] as Set
                )
    }
}

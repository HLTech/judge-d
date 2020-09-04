package com.hltech.judged.server.infrastructure.persistence.environment

import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.Service
import com.hltech.judged.server.domain.environment.Space
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["management.port=0"])
@ActiveProfiles("test-integration")
class JPAEnvironmentRepositoryIT extends Specification {

    @Autowired
    JPAEnvironmentRepository repository

    def 'should retrieve what was persisted'() {
        given:
            def service = new Service('serviceName', 'serviceVersion')
        and:
            def environment = new Environment('environmentName', [new Space('def', [service] as Set)] as Set)
        when:
            repository.persist(environment)
        then:
            def persistedEnvironment = repository.get(environment.name)
            persistedEnvironment.name == environment.name
            persistedEnvironment.allServices[0].name == service.name
            persistedEnvironment.allServices[0].version == service.version

        and:
            noExceptionThrown()
    }

    def 'should overwrite what was persisted before'() {
        given:
            def environment1 = repository.persist(new Environment(
                'environmentName',
                [new Space('def', [new Service('serviceName1', 'serviceVersion1'), new Service('serviceName2', 'serviceVersion2')] as Set)] as Set
            ))
            def environment2 = new Environment(
                environment1.name,
                [ new Space('def', [new Service('serviceName1', 'serviceVersion1')] as Set)] as Set
            )
        when:
            repository.persist(environment2)
        then:
            repository.get(environment1.name).with {
                name == environment1.name
                allServices.size() == 1
            }
    }

    def 'should retrieve names'() {
        given:
            def environment1 = repository.persist(new Environment(
                randomAlphabetic(10),
                [new Space('def', [new Service('serviceName1', 'serviceVersion1'), new Service('serviceName2', 'serviceVersion2')] as Set)] as Set
            ))
        when:
            def names = repository.getNames()
        then:
            names.contains(environment1.name)
    }

    def 'should retrieve service versions of all spaces'(){
        given:
            def environment1 = new Environment(randomAlphabetic(10),
                [
                    new Space('space2', [new Service('s1', 'v1')] as Set),
                    new Space('space', [new Service('s2', 'v2')] as Set)
                ] as Set
            )
        when:
            repository.persist(environment1)
        then:
            repository.get(environment1.name).with {
                name == environment1.name
                allServices.size() == 2
            }
    }
}

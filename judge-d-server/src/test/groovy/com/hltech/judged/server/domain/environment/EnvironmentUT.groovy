package com.hltech.judged.server.domain.environment

import com.hltech.judged.server.domain.ServiceId
import spock.lang.Specification

import static Environment.DEFAULT_NAMESPACE

class EnvironmentUT extends Specification {

    def 'should return services by space'() {
        given:
            def aggregate = new Environment(
            'env',
            [new Space(DEFAULT_NAMESPACE, [new ServiceId("s1", "s1")] as Set), new Space("space1", [new ServiceId("s2", "s2")] as Set)] as Set
        )
        when:
            def defaultSpaceServices = aggregate.getServices(DEFAULT_NAMESPACE)
            def space1Services = aggregate.getServices("space1")
        then:
            defaultSpaceServices[0].name == 's1'
            defaultSpaceServices[0].version == 's1'
            space1Services[0].name == 's2'
            space1Services[0].version == 's2'
    }
}

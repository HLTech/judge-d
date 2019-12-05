package dev.hltech.dredd.domain.environment

import com.google.common.collect.ImmutableMultimap
import dev.hltech.dredd.domain.ServiceVersion
import spock.lang.Specification

class EnvironmentAggregateUT extends Specification {

    def 'should return services by space'() {
        given:
            def aggregate = new EnvironmentAggregate(
            'env',
            ImmutableMultimap.<String, ServiceVersion> builder()
                .put("default", new ServiceVersion("s1", "s1"))
                .put("space1", new ServiceVersion("s2", "s2"))
                .build()
        )
        when:
            def defaultSpaceServiceVersions = aggregate.getServices("default");
            def space1ServiceVersions = aggregate.getServices("space1");
        then:
            defaultSpaceServiceVersions == [new ServiceVersion("s1", "s1")] as Set
            space1ServiceVersions == [new ServiceVersion("s2", "s2")] as Set

    }
}

package dev.hltech.dredd.domain.environment

import spock.lang.Specification

class EnvironmentUT extends Specification {

    def "should delegate findService call to underlying ServiceDiscovery"() {
        given:
            def service1Name = "service1"
            def service2Name = "service2"

            def environment = new Environment(MockServiceDiscovery
                .builder()
                .withProvider(service1Name, "swaggerJson1")
                .withProvider(service2Name, "swaggerJson2")
                .build()
            )
        when:
            def services = environment.findServices(service1Name)
        then:
            services.size() == 1
            (++services.iterator()).name().equalsIgnoreCase(service1Name)

    }

}

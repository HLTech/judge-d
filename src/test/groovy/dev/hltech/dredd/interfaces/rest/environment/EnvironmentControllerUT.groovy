package dev.hltech.dredd.interfaces.rest.environment

import dev.hltech.dredd.domain.Fixtures
import spock.lang.Specification

class EnvironmentControllerUT extends Specification {

    def environmentController = new EnvironmentController(Fixtures.environment())

    def 'should return all services available in environment'() {
        when:
            def services = environmentController.getServices()

        then:
            services.size() == 2
            services[0].name == "dde-instruction-gateway"
            services[0].version == "1.0"
            services[1].name == "frontend"
            services[1].version == "1.0"
    }
}

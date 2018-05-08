package dev.hltech.dredd.interfaces.rest.environment

import dev.hltech.dredd.domain.Fixtures
import spock.lang.Specification

class EnvironmentControllerUT extends Specification {

    def environmentController = new EnvironmentController(Fixtures.environment())

    def 'should return all services available in environment'(){
        when:
            def services = environmentController.getServices()
        then:
            services.size() == 2
    }

    def 'should return provider services marked as provider'(){
        when:
            def services = environmentController.getServices()
        then:
            with(services.find {it.isProvider()}) {
                name == "dde-instruction-gateway"
                version == "1.0"
                consumer == false
            }
    }

    def 'should return consumer services marked as consumer'(){
        when:
        def services = environmentController.getServices()
        then:
        with(services.find {it.isConsumer()}) {
            name == "frontend"
            version == "1.0"
            provider == false
        }
    }


}

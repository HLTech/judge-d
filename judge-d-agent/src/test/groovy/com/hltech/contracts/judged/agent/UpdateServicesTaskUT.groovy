package com.hltech.contracts.judged.agent

import spock.lang.Specification

import static com.hltech.contracts.judged.agent.JudgeDPublisher.*
import static com.hltech.contracts.judged.agent.ServiceLocator.*

class UpdateServicesTaskUT extends Specification {

    UpdateServicesTask task;

    ServiceLocator locator = Mock()
    JudgeDPublisher publisher = Mock()

    def setup(){
        task = new UpdateServicesTask("TEST", locator, publisher)
    }

    def "UpdateServices"() {
        setup:
            locator.locateServices() >> [new Service("service1", "1.0"),new Service("service2", "1.0")]
        when:
            task.updateServices()
        then:
            1* publisher.publish("TEST", {it.size() == 2})

    }
}

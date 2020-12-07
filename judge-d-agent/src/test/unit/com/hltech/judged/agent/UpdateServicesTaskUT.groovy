package com.hltech.judged.agent

import spock.lang.Specification

class UpdateServicesTaskUT extends Specification {

    UpdateServicesTask task;

    ServiceLocator locator = Mock()
    JudgeDPublisher publisher = Mock()

    def setup(){
        task = new UpdateServicesTask("TEST", "default", locator, publisher)
    }

    def "UpdateServices"() {
        setup:
            locator.locateServices() >> [new ServiceLocator.Service("service1", "1.0"), new ServiceLocator.Service("service2", "1.0")]
        when:
            task.updateServices()
        then:
            1* publisher.publish("TEST", 'default', {it.size() == 2})

    }
}

package com.hltech.contracts.judged.agent

import spock.lang.Specification

class UpdateServicesTaskUT extends Specification {

    UpdateServicesTask task;
    ServiceLocator locator;
    JudgeDPublisher publisher;

    def setup(){
        task = new UpdateServicesTask("TEST", locator, publisher);
    }

    def "UpdateServices"() {
        when:
            def a =1
        then:
            a ==1
    }
}

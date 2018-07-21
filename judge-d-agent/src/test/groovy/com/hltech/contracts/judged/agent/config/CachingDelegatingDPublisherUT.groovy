package com.hltech.contracts.judged.agent.config

import com.hltech.contracts.judged.agent.JudgeDPublisher
import spock.lang.Specification

class CachingDelegatingDPublisherUT extends Specification {

    private CachingDelegatingDPublisher publisher
    private JudgeDPublisher targetPublisher

    def setup() {
        targetPublisher = Mock(JudgeDPublisher)
        publisher = new CachingDelegatingDPublisher(targetPublisher)
    }

    def "should delegate to target publisher when publish for the first time"() {
        given:
        def forms = [new JudgeDPublisher.ServiceForm("name", "version")] as Set
        when:
        publisher.publish("SIT", forms)
        then:
        1 * targetPublisher.publish("SIT", forms)
    }

    def "should delegate to target publisher once when subsequently publish the same set of service multiple times "() {
        given:
        def forms = [new JudgeDPublisher.ServiceForm("name", "version")] as Set
        when:
        publisher.publish("SIT", forms)
        publisher.publish("SIT", forms)
        publisher.publish("SIT", forms)
        then:
        1 * targetPublisher.publish("SIT", forms)
    }

    def 'should delegate to target publisher when subsequently publish different set of services'() {
        given:
        def forms1 = [new JudgeDPublisher.ServiceForm("name1", "version")] as Set
        def forms2 = [new JudgeDPublisher.ServiceForm("name2", "version")] as Set
        when:
        publisher.publish("SIT", forms1)
        publisher.publish("SIT", forms2)
        then:
        1 * targetPublisher.publish("SIT", forms1)
        then:
        1 * targetPublisher.publish("SIT", forms2)
    }
}

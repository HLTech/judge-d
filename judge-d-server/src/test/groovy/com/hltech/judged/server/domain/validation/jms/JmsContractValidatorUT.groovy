package com.hltech.judged.server.domain.validation.jms

import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import com.hltech.judged.server.domain.validation.InterfaceContractValidator
import spock.lang.Specification
import spock.lang.Subject

class JmsContractValidatorUT extends Specification {

    @Subject
    def validator = new JmsContractValidator()

    def "Should map raw capabilities"() {
        given:
            def dstType = DestinationType.QUEUE
            def dstName = 'dst'
            def rawCapabilities = """
                [
                    {
                        "destinationType":"${dstType}",
                        "destinationName":"${dstName}",
                        "message": {
                            "type":"string"
                        }
                    }
                ]
            """

        expect:
            validator.asCapabilities(rawCapabilities) == [new Contract(dstType, dstName, new StringSchema())]
    }

    def "Should map raw expectations"() {
        given:
            def dstType = DestinationType.QUEUE
            def dstName = 'dst'
            def rawExpectations = """
                    [
                        {
                            "destinationType":"${dstType}",
                            "destinationName":"${dstName}",
                            "message": {
                                "type":"string"
                            }
                        }
                    ]
                """

        expect:
            validator.asExpectations(rawExpectations) == [new Contract(dstType, dstName, new StringSchema())]
    }

    def "If there are no expectations and capabilities - results are empty"() {
        given:
            def expectations = []

        and:
            def capabilities = []

        when:
            def results = validator.validate(expectations, capabilities)

        then:
            results.size() == 0
    }

    def "If there are no expectations - results are empty"() {
        given:
            def expectations = []

        and:
            def capabilities = [new Contract(DestinationType.QUEUE, 'dst', new StringSchema())]

        when:
            def results = validator.validate(expectations, capabilities)

        then:
            results.size() == 0
    }

    def "If there is matching capability for each expectations - validation results with success"() {
        given:
            def expectations = [new Contract(DestinationType.QUEUE, 'dst', new StringSchema(id: 'id')),
                                new Contract(DestinationType.TOPIC, 'dst', new StringSchema(id: 'id')),
                                new Contract(DestinationType.QUEUE, 'dst2', new StringSchema(id: 'id')),
                                new Contract(DestinationType.QUEUE, 'dst', new NumberSchema(id: 'id2'))]

        and:
            def capabilities = [new Contract(DestinationType.QUEUE, 'dst', new NumberSchema(id: 'id2')),
                                new Contract(DestinationType.TOPIC, 'dst', new StringSchema(id: 'id')),
                                new Contract(DestinationType.QUEUE, 'dst2', new StringSchema(id: 'id')),
                                new Contract(DestinationType.QUEUE, 'dst', new StringSchema(id: 'id')),
                                new Contract(DestinationType.QUEUE, 'weirdDst', new NumberSchema(id: 'id2'))]

        when:
            def results = validator.validate(expectations, capabilities)

        then:
            results.size() == 4
            results.every { result ->
                result.status == InterfaceContractValidator.InteractionValidationStatus.OK
                result.errors.size() == 0
            }
    }

    def "If there are some unmatched expectations - validation results with success"() {
        given:
        def expectations = [new Contract(DestinationType.QUEUE, 'dst', new StringSchema(id: 'id')),
                            new Contract(DestinationType.QUEUE, 'dst2', new StringSchema(id: 'id2')),
                            new Contract(DestinationType.QUEUE, 'dst3', new StringSchema(id: 'id3', pathStart: 'abc')),
                            new Contract(DestinationType.QUEUE, 'dst4', new StringSchema(id: 'id4', pathStart: 'abc'))]

        and:
        def capabilities = [new Contract(DestinationType.TOPIC, 'dst', new StringSchema(id: 'id')),
                            new Contract(DestinationType.QUEUE, 'dst2', new StringSchema(id: 'id22')),
                            new Contract(DestinationType.QUEUE, 'dst3', new StringSchema(id: 'id3', pathStart: 'def')),
                            new Contract(DestinationType.QUEUE, 'dst4', new StringSchema(id: 'id4', pathStart: 'abc'))]

        when:
        def results = validator.validate(expectations, capabilities)

        then:
        results.size() == 4
        results.any { result ->
            result.name == 'Contract(destinationType = QUEUE, destinationName = "dst", messageId = "id")'
            result.status == InterfaceContractValidator.InteractionValidationStatus.FAILED
            result.errors.size() == 1
            result.errors[0] == 'Missing endpoint required by consumer'
        }
        results.any { result ->
            result.name == 'Contract(destinationType = QUEUE, destinationName = "dst2", messageId = "id2")'
            result.status == InterfaceContractValidator.InteractionValidationStatus.FAILED
            result.errors.size() == 1
            result.errors[0] == 'Missing message with given id required by consumer'
        }
        results.any { result ->
            result.name == 'Contract(destinationType = QUEUE, destinationName = "dst3", messageId = "id3")'
            result.status == InterfaceContractValidator.InteractionValidationStatus.FAILED
            result.errors.size() == 1
            result.errors[0] == 'Schema with id id3 has not matching pathStart - consumer: abc, provider: def'
        }
        results.any { result ->
            result.name == 'Contract(destinationType = QUEUE, destinationName = "dst4", messageId = "id4")'
            result.status == InterfaceContractValidator.InteractionValidationStatus.OK
            result.errors.size() == 0
        }
    }
}

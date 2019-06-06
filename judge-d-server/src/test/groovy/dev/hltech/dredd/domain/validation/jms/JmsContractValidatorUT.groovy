package dev.hltech.dredd.domain.validation.jms

import com.fasterxml.jackson.module.jsonSchema.types.NullSchema
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import dev.hltech.dredd.domain.validation.InterfaceContractValidator
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
                        "body": {
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
                            "body": {
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
            def expectations = [new Contract(DestinationType.QUEUE, 'dst', new StringSchema()),
                                new Contract(DestinationType.TOPIC, 'dst', new StringSchema()),
                                new Contract(DestinationType.QUEUE, 'dst2', new StringSchema()),
                                new Contract(DestinationType.QUEUE, 'dst', new NumberSchema())]

        and:
            def capabilities = [new Contract(DestinationType.QUEUE, 'dst', new NumberSchema()),
                                new Contract(DestinationType.TOPIC, 'dst', new StringSchema()),
                                new Contract(DestinationType.QUEUE, 'dst2', new StringSchema()),
                                new Contract(DestinationType.QUEUE, 'dst', new StringSchema()),
                                new Contract(DestinationType.QUEUE, 'weirdDst', new NumberSchema())]

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
        def expectations = [new Contract(DestinationType.QUEUE, 'dst', new StringSchema()),
                            new Contract(DestinationType.TOPIC, 'dst', new StringSchema()),
                            new Contract(DestinationType.QUEUE, 'dst2', new StringSchema()),
                            new Contract(DestinationType.QUEUE, 'dst', new NumberSchema()),
                            new Contract(DestinationType.QUEUE, 'weirdDst', new NumberSchema()),
                            new Contract(DestinationType.QUEUE, 'dst', new NullSchema())]

        and:
        def capabilities = [new Contract(DestinationType.QUEUE, 'dst', new NumberSchema()),
                            new Contract(DestinationType.TOPIC, 'dst', new StringSchema()),
                            new Contract(DestinationType.QUEUE, 'dst2', new StringSchema()),
                            new Contract(DestinationType.QUEUE, 'dst', new StringSchema())]

        when:
        def results = validator.validate(expectations, capabilities)

        then:
        results.size() == 6
        results.any { result ->
            result.status == InterfaceContractValidator.InteractionValidationStatus.FAILED
            result.errors.size() == 1
            result.errors[0] == 'Missing endpoint required by consumer'
        }
        results.any { result ->
            result.status == InterfaceContractValidator.InteractionValidationStatus.FAILED
            result.errors.size() == 1
            result.errors[0] == 'Wrong schema of the message'
        }
        results.count { result ->
            result.status == InterfaceContractValidator.InteractionValidationStatus.OK
            result.errors.size() == 0
        } == 4
    }
}

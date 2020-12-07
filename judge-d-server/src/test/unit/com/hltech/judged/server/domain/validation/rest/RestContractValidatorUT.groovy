package com.hltech.judged.server.domain.validation.rest

import au.com.dius.pact.model.RequestResponsePact
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.io.ByteStreams.toByteArray
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

class RestContractValidatorUT extends Specification {

    RestContractValidator validator = new RestContractValidator()

    def "should convert rawExpectations to pact"() {
        given:
            def pactAsString = new String(toByteArray(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json")))
        when:
            def pact = validator.asExpectations(pactAsString)
        then:
            pact == loadPact(pactAsString)

    }

    def "should return rawCapabilities as Capabilities"() {
        given:
            def rawCapabilities = randomAlphanumeric(100)
        when:
            def capabilities = validator.asCapabilities(rawCapabilities)
        then:
            capabilities == rawCapabilities
    }

    def 'should return validation result with all interactions validated when validate swagger with pact'() {
        given:
            def swagger = new String(toByteArray(getClass().getResourceAsStream("/swagger-backend-provider.json")))
            def pact = (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
        when:
            def validationResult = validator.validate(pact, swagger)
        then:
            validationResult.size() == 1
            with(validationResult.get(0)) {
                status == FAILED
                name == "a request for details"
                errors.size() == 1
            }
    }

    def 'should not return validation errors when validate given consumer ignores body'() {
        given:
            def swagger = new String(toByteArray(getClass().getResourceAsStream("/swagger-ignored-response-body.json")))
            def pact = (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact-ignored-response-body.json"))
        when:
            def validationResult = validator.validate(pact, swagger)
        then:
            for (report in validationResult) {
                with(report) {
                    status == OK
                }
            }
    }

    def 'should validate contracts specified in openapi v3 - should find failures when media type is specified'() {
        given:
            def swagger = new String(toByteArray(getClass().getResourceAsStream("/swagger-openapi3.json")))
            def pact = (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact.json"))
        when:
            def validationResult = validator.validate(pact, swagger)
        then:
            validationResult.size() == 2
            validationResult.any { result ->
                result.name == 'get envs; 200 OK response' &&
                    result.status == FAILED &&
                    result.errors.get(0) == "[Path '/0'] Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"
            }
            validationResult.any { result ->
                result.name == 'publish request; 200 OK response' &&
                    result.status == FAILED &&
                    result.errors.get(0) == "[Path '/0'] Object instance has properties which are not allowed by the schema: [\"gyyigi\"]"
            }
    }
}

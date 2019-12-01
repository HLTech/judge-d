package dev.hltech.dredd.domain.validation.rest

import au.com.dius.pact.model.RequestResponsePact
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.io.ByteStreams.toByteArray
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric

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
}

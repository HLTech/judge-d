package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.io.ByteStreams.toByteArray

class PactValidatorUT extends Specification {

    private PactValidator validator

    void setup() {
        def environment = StaticEnvironment.builder()
            .withProvider(
            "instruction-gateway",
            new String(toByteArray(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json"))))
            .build()
        validator = new PactValidator(environment)
    }

    def 'should verify all interactions from pact file'() {
        given:
            def pact = loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-engine.json"))
        when:
            def validationReports = validator.validate(pact)
        then:
            with(validationReports.get(0)) {
                status == ValidationStatus.FAILED
                name.equalsIgnoreCase("a request for instruction details")
                errors.size() == 2
            }
    }

}

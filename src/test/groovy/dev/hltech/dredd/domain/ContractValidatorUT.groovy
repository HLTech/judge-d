package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.Environment
import dev.hltech.dredd.domain.environment.MockServiceDiscovery
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.io.ByteStreams.toByteArray

class ContractValidatorUT extends Specification {

    private ContractValidator verifier

    void setup() {
        def environment = new Environment(MockServiceDiscovery.builder()
            .withProvider(
            "instruction-gateway",
            new String(toByteArray(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json"))))
            .build())
        verifier = new ContractValidator(environment)
    }

    def 'should verify all interactions from pact file'() {
        given:
            def pact = loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-engine.json"))
        when:
            def validationReports = verifier.validate(pact)
        then:
            with(validationReports.get(0)) {
                status == ValidationStatus.FAILED
                name.equalsIgnoreCase("a request for instruction details")
                errors.size() == 2
            }
    }

}

package dev.hltech.dredd.domain.validation

import spock.lang.Specification

class InterfaceContractValidatorTest extends Specification {

    def validator = [
        asCapabilities: { rawCapabilities ->
            return rawCapabilities
        },
        asExpectations: { rawExpectations ->
            return rawExpectations
        },
        validate      : { expectations, capabilities ->
            return null
        }
    ] as InterfaceContractValidator

    def "Validate capabilities"() {
    }

    def "Validate1 expectations"() {
    }
}

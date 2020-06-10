package com.hltech.judged.server.interfaces.rest.validation


import com.hltech.judged.server.domain.validation.InterfaceContractValidator
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED
import static com.hltech.judged.server.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK
import static Converters.toDtos

class ConvertersUT extends Specification {

    def "should merge interactions for all available communication interfaces"() {
        given:
            def ping1EnvironmentValidatorResult = new com.hltech.judged.server.domain.validation.EnvironmentValidatorResult(
                "ping",
                newArrayList(new InterfaceContractValidator.CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InterfaceContractValidator.InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new InterfaceContractValidator.ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InterfaceContractValidator.InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
            def ping2EnvironmentValidatorResult = new com.hltech.judged.server.domain.validation.EnvironmentValidatorResult(
                "ping2",
                newArrayList(new InterfaceContractValidator.CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InterfaceContractValidator.InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new InterfaceContractValidator.ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InterfaceContractValidator.InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
        when:
            def converted = toDtos(new com.hltech.judged.server.domain.ServiceVersion("validated-service", "1.0"),
                    newArrayList(ping1EnvironmentValidatorResult, ping2EnvironmentValidatorResult)
            )
        then:
            converted.size() == 2
            converted[0].getConsumerAndProvider() == new ConsumerAndProviderDto("consumer", "version", "validated-service", "1.0")
            converted[0].getInteractions().size() == 2
            converted[0].getInteractions()[0].validationResult == OK
            converted[0].getInteractions()[0].communicationInterface == "ping"
            converted[0].getInteractions()[0].interactionName == "some-interaction-1"
            converted[0].getInteractions()[0].errors.isEmpty()
            converted[0].getInteractions()[1].validationResult == OK
            converted[0].getInteractions()[1].communicationInterface == "ping2"
            converted[0].getInteractions()[1].interactionName == "some-interaction-1"
            converted[0].getInteractions()[1].errors.isEmpty()

            converted[1].getConsumerAndProvider() == new ConsumerAndProviderDto("validated-service", "1.0", "provider", "1.0")
            converted[1].getInteractions().size() == 2
            converted[1].getInteractions()[0].validationResult == FAILED
            converted[1].getInteractions()[0].communicationInterface == "ping"
            converted[1].getInteractions()[0].interactionName == "some-interaction-2"
            converted[1].getInteractions()[0].errors == ["because"] as List
            converted[1].getInteractions()[1].validationResult == FAILED
            converted[1].getInteractions()[1].communicationInterface == "ping2"
            converted[1].getInteractions()[1].interactionName == "some-interaction-2"
            converted[1].getInteractions()[1].errors == ["because"] as List
    }

    def "should include both capabilitiesValidationResults and expectationsValidationResults"() {
        given:
            def environmentValidatorResult = new com.hltech.judged.server.domain.validation.EnvironmentValidatorResult(
                "ping",
                newArrayList(new InterfaceContractValidator.CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InterfaceContractValidator.InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new InterfaceContractValidator.ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InterfaceContractValidator.InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
        when:
            def converted = toDtos(new com.hltech.judged.server.domain.ServiceVersion("validated-service", "1.0"),
                    newArrayList(environmentValidatorResult)
            )
        then:
            converted.size() == 2
            converted[0].getConsumerAndProvider() == new ConsumerAndProviderDto("consumer", "version", "validated-service", "1.0")
            converted[0].getInteractions().size() == 1
            converted[0].getInteractions()[0].validationResult == OK
            converted[0].getInteractions()[0].communicationInterface == "ping"
            converted[0].getInteractions()[0].interactionName == "some-interaction-1"
            converted[0].getInteractions()[0].errors.isEmpty()

            converted[1].getConsumerAndProvider() == new ConsumerAndProviderDto("validated-service", "1.0", "provider", "1.0")
            converted[1].getInteractions().size() == 1
            converted[1].getInteractions()[0].validationResult == FAILED
            converted[1].getInteractions()[0].communicationInterface == "ping"
            converted[1].getInteractions()[0].interactionName == "some-interaction-2"
            converted[1].getInteractions()[0].errors == ["because"] as List
    }
}

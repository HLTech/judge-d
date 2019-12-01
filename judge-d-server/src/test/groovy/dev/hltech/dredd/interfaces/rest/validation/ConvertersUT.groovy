package dev.hltech.dredd.interfaces.rest.validation

import dev.hltech.dredd.domain.ServiceVersion
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.*
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.FAILED
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK
import static dev.hltech.dredd.interfaces.rest.validation.Converters.toDtos

class ConvertersUT extends Specification {

    def "should merge interactions for all available communication interfaces"() {
        given:
            def ping1EnvironmentValidatorResult = new EnvironmentValidatorResult(
                "ping",
                newArrayList(new CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
            def ping2EnvironmentValidatorResult = new EnvironmentValidatorResult(
                "ping2",
                newArrayList(new CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
        when:
            def converted = toDtos(
                newArrayList(ping1EnvironmentValidatorResult, ping2EnvironmentValidatorResult),
                new ServiceVersion("validated-service","1.0")
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
            def environmentValidatorResult = new EnvironmentValidatorResult(
                "ping",
                newArrayList(new CapabilitiesValidationResult(
                    "consumer",
                    "version",
                    newArrayList(
                        InteractionValidationResult.success("some-interaction-1")
                    ))
                ),
                newArrayList(new ExpectationValidationResult(
                    "provider",
                    "1.0",
                    newArrayList(InteractionValidationResult.fail("some-interaction-2", newArrayList("because")))
                ))
            )
        when:
            def converted = toDtos(
                newArrayList(environmentValidatorResult),
                new ServiceVersion("validated-service","1.0")
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

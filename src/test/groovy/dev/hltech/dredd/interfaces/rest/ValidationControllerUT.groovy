package dev.hltech.dredd.interfaces.rest

import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.domain.PactValidator
import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.io.ByteStreams.toByteArray
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.NO_SUCH_PROVIDER_ON_ENVIRONMENT
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.OK

class ValidationControllerUT extends Specification {

    private ObjectMapper objectMapper

    void setup(){
        this.objectMapper = new ObjectMapper()
    }

    def 'should return provider-not-found message in response when provider doesnt exist in environment' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
            def environment = StaticEnvironment.builder()
                .withProvider(
                "some service",
                "1.0",
                "{}"
            ).build()
        when:
            def controller = new ValidationController(new PactValidator(environment), objectMapper)
            def validationResult = controller.validatePacts(new PactValidationForm(newArrayList((Object)pact)))

        then:
            with(validationResult) {
                validationResults.size() == 1
                validationResults.get(0).validationStatus == NO_SUCH_PROVIDER_ON_ENVIRONMENT
            }
    }

    def 'should return validation result with all interactions' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
            def environment = StaticEnvironment.builder()
                    .withProvider(
                    "backend-provider",
                    "1.0",
                    getClass().getResourceAsStream("/backend-provider-swagger.json")
                ).build()

        when:
            def validationController = new ValidationController(new PactValidator(environment), objectMapper)
            def validationResult = validationController.validatePacts(new PactValidationForm(newArrayList((Object)pact)))
        then:
            with (validationResult) {
                validationResults.size() == 1
                validationResults.get(0).validationStatus == OK
                validationResults.get(0).consumerName == "frontend"
                validationResults.get(0).providerName == "backend-provider"
            }
    }
}

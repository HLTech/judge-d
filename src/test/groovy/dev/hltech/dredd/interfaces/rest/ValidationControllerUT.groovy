package dev.hltech.dredd.interfaces.rest

import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.domain.PactValidator
import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.PERFORMED

class ValidationControllerUT extends Specification {

    private ObjectMapper objectMapper

    void setup(){
        this.objectMapper = new ObjectMapper()
    }

    def 'should return provider-not-found message in response when provider doesnt exist in environment' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
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
                validationResults.get(0).validationStatus == FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT
            }
    }

    def 'should return validation result with all interactions' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            def environment = StaticEnvironment.builder()
                    .withProvider(
                    "instruction-gateway",
                    "1.0",
                    getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")
                ).build()

        when:
            def validationController = new ValidationController(new PactValidator(environment), objectMapper)
            def validationResult = validationController.validatePacts(new PactValidationForm(newArrayList((Object)pact)))
        then:
            with (validationResult) {
                validationResults.size() == 1
                validationResults.get(0).validationStatus == PERFORMED
                validationResults.get(0).consumerName == "frontend"
                validationResults.get(0).providerName == "instruction-gateway"
            }
    }
}

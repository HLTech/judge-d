package dev.hltech.dredd.interfaces.rest

import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.domain.PactValidator
import dev.hltech.dredd.domain.SwaggerValidator
import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.collect.Lists.newArrayList
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.PERFORMED

class ValidationControllerUT extends Specification {

    private ObjectMapper objectMapper

    void setup(){
        this.objectMapper = new ObjectMapper()
    }

    def 'should return provider-not-found message in response when validate pact given provider doesnt exist in environment' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            def environment = StaticEnvironment.builder()
                .withProvider(
                "some service",
                "1.0",
                "{}"
            ).build()
        when:
            def controller = new ValidationController(new PactValidator(environment), new SwaggerValidator(environment), objectMapper)
            def form = new PactValidationForm()
            form.setPacts(newArrayList((Object) pact))
            def validationResult = controller.validatePacts(form)

        then:
            with(validationResult) {
                validationResults.size() == 1
                validationResults.get(0).validationStatus == FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT
            }
    }

    def 'should return validation result with all interactions when validate pact given provider is available' (){
        given:
            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            def environment = StaticEnvironment.builder()
                    .withProvider(
                    "instruction-gateway",
                    "1.0",
                    getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")
                ).build()

        when:
            def validationController = new ValidationController(new PactValidator(environment), new SwaggerValidator(environment), objectMapper)
            def form = new PactValidationForm()
            form.setPacts(newArrayList((Object)pact))
            def validationResult = validationController.validatePacts(form)
        then:
            with (validationResult) {
                validationResults.size() == 1
                validationResults.get(0).validationStatus == PERFORMED
                validationResults.get(0).consumerName == "frontend"
                validationResults.get(0).providerName == "instruction-gateway"
            }
    }

    def 'should return validation result with all interactions when validate swagger given consumer is available' (){
        given:
            def swagger = objectMapper.readTree(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json"))
            def environment = StaticEnvironment.builder()
                .withConsumer(
                "frontend",
                "1.0",
                newArrayList(loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json")))
            ).build()
        when:
            def validationController = new ValidationController(new PactValidator(environment), new SwaggerValidator(environment), objectMapper)
            def form = new SwaggerValidationForm()
                form.setProviderName("dde-instruction-gateway")
                form.setSwagger(swagger)
            def validationResult = validationController.validateSwagger(form)
        then:
        with (validationResult) {
            validationResults.size() == 1
            validationResults.get(0).validationStatus == PERFORMED
            validationResults.get(0).consumerName == "frontend"
            validationResults.get(0).consumerVersion == "1.0"
            validationResults.get(0).providerName == "dde-instruction-gateway"
        }
    }
}

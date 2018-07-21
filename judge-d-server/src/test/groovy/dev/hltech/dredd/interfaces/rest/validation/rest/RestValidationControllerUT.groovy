package dev.hltech.dredd.interfaces.rest.validation.rest

import au.com.dius.pact.model.RequestResponsePact
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import dev.hltech.dredd.domain.validation.JudgeD
import dev.hltech.dredd.domain.validation.JudgeD.ContractValidator
import dev.hltech.dredd.domain.validation.ProviderNotAvailableException
import dev.hltech.dredd.domain.validation.rest.RestContractValidator
import spock.lang.Specification

import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.*
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.OK
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric

class RestValidationControllerUT extends Specification {

    private JudgeD judgeD = Mock()
    private ContractValidator<String, RequestResponsePact> contractValidator = Mock()
    private JsonNodeFactory factory

    private RestValidationController controller

    void setup(){
        controller = new RestValidationController(new ObjectMapper(), judgeD, new RestContractValidator())
        factory = new JsonNodeFactory()
    }

    def ''() {
        setup:
            def validationResult = new CapabilitiesValidationResult(
                "consumer",
                "version",
                [new InteractionValidationResult("interaction", OK, ["OK"] as List)] as List
            )
            def form = new SwaggerValidationForm("some-provider", factory.objectNode())

            judgeD.createContractValidator(_, _) >> contractValidator
            contractValidator.validateCapabilities(_, _) >> [validationResult]
        when:
            def aggregatedValidationResult = controller.validateSwagger("SIT", form)
        then:
            aggregatedValidationResult.validationResults.size() == 1
            with(aggregatedValidationResult.validationResults.get(0)) {
                consumerName == validationResult.consumerName
                consumerVersion == validationResult.consumerVersion
                providerName == "some-provider"
                providerVersion == null
                interactions.size() == 1
            }
    }

    def '1'(){
        setup:
            judgeD.createContractValidator(_, _) >> contractValidator
            contractValidator.validateExpectations(_, _) >> {throw new ProviderNotAvailableException()}
        when:
            def aggregatedValidationResult = controller.validatePacts("SIT", new PactValidationForm([randomPact("some-other-provider-" + randomAlphanumeric(10), "consumer-" + randomAlphabetic(10)), randomPact("some-other-provider-" + randomAlphanumeric(10), "consumer-" + randomAlphabetic(10))] as List))
        then:
            aggregatedValidationResult.validationResults.size() == 2
    }

    def '2'(){
        setup:
            def consumerName = "consumer-" + randomAlphabetic(10)
            def providerName1 = "some-other-provider-" + randomAlphanumeric(10)
            def providerName2 = "some-other-provider-" + randomAlphanumeric(10)
            judgeD.createContractValidator(_, _) >> contractValidator
            contractValidator.validateExpectations(providerName1, _) >> [randomExpectationsValidationResult(providerName1, "version1")]
            contractValidator.validateExpectations(providerName2, _) >> [randomExpectationsValidationResult(providerName2, "version1")]
        when:
            def aggregatedValidationResult = controller.validatePacts(
                "SIT",
                new PactValidationForm([
                    randomPact(providerName1, consumerName),
                    randomPact(providerName2, consumerName)
                ] as List)
            )
        then:
            aggregatedValidationResult.validationResults.size() == 2
    }

    private ExpectationValidationResult randomExpectationsValidationResult(String providerName, String version) {
        new ExpectationValidationResult(
            providerName,
            version,
            [new InteractionValidationResult("interaction", OK, ["OK"] as List)] as List
        )
    }

    private ObjectNode randomPact(String providerName, String consumerName) {
        def pact = factory.objectNode()
        def provider = factory.objectNode()
        def consumer = factory.objectNode()
        provider.set("name", factory.textNode(providerName))
        consumer.set("name", factory.textNode(consumerName))
        pact.set("provider", provider)
        pact.set("consumer", consumer)
        return pact
    }

//    def 'should return provider-not-found message in response when validate pact given provider doesnt exist in environment' (){
//        given:
//            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
//            def environment = StaticEnvironment.builder()
//                .withProvider(
//                "some service",
//                "1.0",
//                "{}"
//            ).build()
//        when:
//            def controller = new RestValidationController(objectMapper)
//            def form = new PactValidationForm()
//            form.setPacts(newArrayList((Object) pact))
//            def validationResult = controller.validatePacts(form)
//
//        then:
//            with(validationResult) {
//                validationResults.size() == 1
//                validationResults.get(0).validationStatus == FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT
//            }
//    }
//
//    def 'should return validation result with all interactions when validate pact given provider is available' (){
//        given:
//            def pact = objectMapper.readTree(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
//            def environment = StaticEnvironment.builder()
//                    .withProvider(
//                    "instruction-gateway",
//                    "1.0",
//                    getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")
//                ).build()
//
//        when:
//            def validationController = new RestValidationController(objectMapper)
//            def form = new PactValidationForm()
//            form.setPacts(newArrayList((Object)pact))
//            def validationResult = validationController.validatePacts(form)
//        then:
//            with (validationResult) {
//                validationResults.size() == 1
//                validationResults.get(0).validationStatus == PERFORMED
//                validationResults.get(0).consumerName == "frontend"
//                validationResults.get(0).providerName == "instruction-gateway"
//            }
//    }
//
//    def 'should return validation result with all interactions when validate swagger given consumer is available' (){
//        given:
//            def swagger = objectMapper.readTree(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json"))
//            def environment = StaticEnvironment.builder()
//                .withConsumer(
//                "frontend",
//                "1.0",
//                newArrayList(loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json")))
//            ).build()
//        when:
//            def validationController = new RestValidationController(objectMapper)
//            def form = new SwaggerValidationForm()
//                form.setProviderName("instruction-gateway")
//                form.setSwagger(swagger)
//            def validationResult = validationController.validateSwagger(form)
//        then:
//        with (validationResult) {
//            validationResults.size() == 1
//            validationResults.get(0).validationStatus == PERFORMED
//            validationResults.get(0).consumerName == "frontend"
//            validationResults.get(0).consumerVersion == "1.0"
//            validationResults.get(0).providerName == "instruction-gateway"
//        }
//    }
}

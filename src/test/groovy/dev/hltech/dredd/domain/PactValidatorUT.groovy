package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact

class PactValidatorUT extends Specification {

    def 'should throw ProviderNotFoundException when verify pact given provider doesnt exist' (){
        given:
            def environment =StaticEnvironment.builder()
                .withProvider("some-service","1.0",getClass().getResourceAsStream("/backend-provider-swagger.json"))
                .build()
            def pact = loadPact(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
        when:
            new PactValidator(environment).validate(pact)
        then:
            thrown ProviderNotAvailableException
    }

    def 'should verify all interactions from pact file'() {
        given:
            def environment = StaticEnvironment.builder()
                .withProvider("backend-provider", "1.0", getClass().getResourceAsStream("/backend-provider-swagger.json"))
                .build()
            def pact = loadPact(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
        when:
            def pactValidationReports = new PactValidator(environment).validate(pact)
        then:
            pactValidationReports.size() == 1
            with(pactValidationReports.get(0)) {
                consumerName == "frontend"
                interactionValidationReports.size() == 1
                interactionValidationReports.get(0).status == InteractionValidationReport.InteractionValidationStatus.FAILED
                interactionValidationReports.get(0).name == "a request for details"
                interactionValidationReports.get(0).errors.size() == 2
            }
    }

    def 'should verify all interaction against all matching providers' (){
        given:
            def environment = StaticEnvironment.builder()
                .withProvider("instruction-gateway", "1.0", getClass().getResourceAsStream("/backend-provider-swagger.json"))
                .withProvider("instruction-gateway", "2.0", getClass().getResourceAsStream("/backend-provider-swagger.json"))
                .build()
            def pact = loadPact(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json"))
        when:
            def pactValidationReports = new PactValidator(environment).validate(pact)
        then:
            pactValidationReports.size() == 2
            with(pactValidationReports.find {it.providerVersion == "1.0"}) {
                consumerName == "frontend"
                interactionValidationReports.size() == 1
                interactionValidationReports.get(0).status == InteractionValidationReport.InteractionValidationStatus.FAILED
                interactionValidationReports.get(0).name == "a request for details"
                interactionValidationReports.get(0).errors.size() == 2
            }
            with(pactValidationReports.find {it.providerVersion == "2.0"}) {
                consumerName == "frontend"
                interactionValidationReports.size() == 1
                interactionValidationReports.get(0).status == InteractionValidationReport.InteractionValidationStatus.FAILED
                interactionValidationReports.get(0).name == "a request for details"
                interactionValidationReports.get(0).errors.size() == 2
            }
    }

}

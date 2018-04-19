package dev.hltech.dredd.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.hltech.dredd.domain.environment.StaticEnvironment
import spock.lang.Specification

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.collect.Lists.newArrayList

class SwaggerValidatorUT extends Specification {

    def 'should return empty list of reports when validate given no consumers found on environment'(){
        given:
            def environment = StaticEnvironment.builder().build()
            def swagger = loadJson("/dde-instruction-gateway-swagger.json")
        when:
            def reports = new SwaggerValidator(environment).validate("instruction-gateway", swagger)
        then:
            reports.empty
    }

    def 'should return one report for each consumer when validate given multiple consumers were found'(){
        given:
            def environment = StaticEnvironment.builder()
                .withConsumer("frontend", "1.0", newArrayList((Object)loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))))
                .withConsumer("frontend", "2.0", newArrayList((Object)loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))))
                .build()
            def swagger = loadJson("/dde-instruction-gateway-swagger.json")
        when:
            def reports = new SwaggerValidator(environment).validate("instruction-gateway", swagger)
        then:
            reports.size() == 2
            reports.find {it.consumerName == "frontend" && it.consumerVersion=="1.0"}
            reports.find {it.consumerName == "frontend" && it.consumerVersion=="2.0"}
    }

    JsonNode loadJson(String location) {
        new ObjectMapper().readTree(getClass().getResourceAsStream(location))
    }
}

package com.hltech.judged.server

import io.restassured.RestAssured
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

import static com.hltech.judged.server.FileHelper.loadFromFileAndFormat

@FunctionalTest
class InterrelationshipControllerFT extends PostgresDatabaseSpecification {

    @LocalServerPort
    int serverPort

    @Sql('InterrelationshipControllerFT.WithContracts.sql')
    def 'should get interrelationship for selected environment for service with published contracts'() {
        given:
            def environmentName = 'TEST'

        and:
            def expectations = loadFromFileAndFormat('pact-frontend-to-backend-provider.json')
            def capabilities = loadFromFileAndFormat('swagger-backend-provider.json')

        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/interrelationship/${environmentName}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getMap('$')

        then:
            response['environment'] == 'TEST'
            response['serviceContracts'][0]['name'] == 'test-service'
            response['serviceContracts'][0]['version'] == "1.0"
            response['serviceContracts'][0]['publication-time']
            response['serviceContracts'][0]['capabilities']['rest']['mimeType'] == "application/json"
            response['serviceContracts'][0]['capabilities']['rest']['value'] == capabilities
            response['serviceContracts'][0]['expectations']['test-service']['rest']['mimeType'] == "application/json"
            response['serviceContracts'][0]['expectations']['test-service']['rest']['value'] == expectations
    }

    @Sql('InterrelationshipControllerFT.WithoutContracts.sql')
    def 'should get interrelationship for selected environment for service without published contracts'() {
        given:
        def environmentName = 'TEST'

        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/interrelationship/${environmentName}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getMap('$')

        then:
            response['environment'] == 'TEST'
            response['serviceContracts'][0]['name'] == 'test-service'
            response['serviceContracts'][0]['version'] == "1.0"
            response['serviceContracts'][0]['publication-time'] == null
            response['serviceContracts'][0]['capabilities']['rest']['value'] == []
            response['serviceContracts'][0]['expectations']['test-service']['rest']['value'] == []
    }

    def 'should return 404 if get selected environment doesn`t exists'() {
        expect:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/interrelationship/NOT_EXISTS")
                .then()
                .statusCode(404)
    }
}

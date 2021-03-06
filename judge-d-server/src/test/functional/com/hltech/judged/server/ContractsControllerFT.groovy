package com.hltech.judged.server

import io.restassured.RestAssured
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

import java.time.Instant

import static com.hltech.judged.server.FileHelper.loadFromFileAndFormat
import static java.time.temporal.ChronoUnit.MILLIS

@FunctionalTest
class ContractsControllerFT extends PostgresDatabaseSpecification {

    @LocalServerPort
    int serverPort

    def 'should register contracts for a version of a service'() {
        given:
            def expectations = loadFromFileAndFormat('pact-frontend-to-backend-provider.json')
            def capabilities = loadFromFileAndFormat('swagger-backend-provider.json')

        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .body("""
                {
                    "capabilities":{
                        "rest":{"value":"${capabilities.replace('"','\\"')}","mimeType":"application/json"}
                    },
                    "expectations":{
                        "test-service":{"rest":{"value":"${expectations.replace('"','\\"')}","mimeType":"application/json"}}
                    }
                }
                """)
                .when()
                    .post("/contracts/services/test-service/versions/1.0")
                .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract().body().jsonPath().getMap('$')

        then:
                        def capabilitiesFromResponse = response['capabilities']['rest']
            capabilitiesFromResponse['mimeType'] == "application/json"
            capabilitiesFromResponse['value'] == capabilities

            def expectationsFromResponse = response['expectations']['test-service']['rest']
            expectationsFromResponse['mimeType'] == "application/json"
            expectationsFromResponse['value'] == expectations

        and:
            response['publicationTime']
        and:
            def capabilitiesFromDb = dbHelper.fetchCapabilities()
            capabilitiesFromDb.size() == 1
            capabilitiesFromDb[0]['service_name'] == 'test-service'
            capabilitiesFromDb[0]['service_version'] == "1.0"
            capabilitiesFromDb[0]['protocol'] == "rest"
            capabilitiesFromDb[0]['mime_type'] == "application/json"
            capabilitiesFromDb[0]['value'] == capabilities

            def expectationsFromDb = dbHelper.fetchExpectations()
            capabilitiesFromDb.size() == 1
            expectationsFromDb[0]['service_name'] == 'test-service'
            expectationsFromDb[0]['service_version'] == "1.0"
            expectationsFromDb[0]['protocol'] == "rest"
            expectationsFromDb[0]['mime_type'] == "application/json"
            expectationsFromDb[0]['value'] == expectations

            def serviceContractsFromDb = dbHelper.fetchServiceContracts()
            serviceContractsFromDb.size() == 1
            serviceContractsFromDb[0]['name'] == 'test-service'
            serviceContractsFromDb[0]['version'] == "1.0"
            serviceContractsFromDb[0]['publication_time'].toInstant().truncatedTo( MILLIS ) == Instant.parse(response['publicationTime']).truncatedTo( MILLIS )
    }

    @Sql('ContractsControllerFT.GeRegisteredContracts.sql')
    def 'should get registered contracts for service version'() {
        given:
            def expectations = loadFromFileAndFormat('pact-frontend-to-backend-provider.json')
            def capabilities = loadFromFileAndFormat('swagger-backend-provider.json')

        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service/versions/1.0")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getMap('$')

        then:
            response['name'] == 'test-service'
            response['version'] == "1.0"
            def capabilitiesFromResponse = response['capabilities']['rest']
            capabilitiesFromResponse['mimeType'] == "application/json"
            capabilitiesFromResponse['value'] == capabilities

            def expectationsFromResponse = response['expectations']['test-service']['rest']
            expectationsFromResponse['mimeType'] == "application/json"
            expectationsFromResponse['value'] == expectations
    }

    @Sql('ContractsControllerFT.GeRegisteredContracts.sql')
    def "should return 404 if service don't have registered contracts for service version"() {
        expect:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service/versions/'9.9")
                .then()
                .statusCode(404)
                .contentType("application/json")
    }

    @Sql('ContractsControllerFT.GeRegisteredServices.sql')
    def 'should get registered services names'() {
        when:
            def contracts = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            contracts.size() == 4
            contracts.containsAll(["test-service", "test-provider2", "test-provider3", "test-provider4"])

        where:
            path << ["/contracts", "/contracts/services"]
    }

    @Sql('ContractsControllerFT.GetVersions.sql')
    def 'should get registered versions for service'() {
        when:
            def versions = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service/versions")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            versions.size() == 4
            versions.containsAll(["1.0", "1.2", "2.0", "3.0"])
    }

    @Sql('ContractsControllerFT.GetVersions.sql')
    def "should return empty list if service doesn't have any version registered"() {
        when:
            def versions = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-provider9/versions")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            versions.size() == 0
    }

    @Sql('ContractsControllerFT.GeRegisteredContracts.sql')
    def 'should get registered capabilities for service version and protocol'() {
        given:
            def capabilities = loadFromFileAndFormat('swagger-backend-provider.json')

        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service/versions/1.0/capabilities/rest")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().asString()

        then:
            response == capabilities
    }

    @Sql('ContractsControllerFT.GeRegisteredContracts.sql')
    def "should return 404 if service doesn't have registered capabilities for protocol"() {
        expect:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service/versions/'1.0'/capabilities/jms")
                .then()
                .statusCode(404)
                .contentType("application/json")
    }

    @Sql('ContractsControllerFT.GeRegisteredServices.sql')
    def 'should return 200 if server have registered contracts'() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-service")
                .then()
                .statusCode(200)
                .contentType("text/plain;charset=UTF-8")
                .extract().body().asString()

        then:
            response == "test-service"
    }

    @Sql('ContractsControllerFT.GeRegisteredServices.sql')
    def 'should return 404 if server not have registered contracts'() {
        expect:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/contracts/services/test-provider9")
                .then()
                .statusCode(404)
    }
}

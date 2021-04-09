package com.hltech.judged.server

import io.restassured.RestAssured
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

@Sql
@FunctionalTest
class ValidationControllerFT extends PostgresDatabaseSpecification {

    @LocalServerPort
    int serverPort

    def "should return services validation result for environment"() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get('/environment-compatibility-report?services=test-service-1:1.0&services=test-service-2:2.0&environment=TEST1')
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.any {
                it['service']['name'] == 'test-service-1' &&
                it['service']['version'] == '1.0' &&
                it['validationReports'][0]['consumerAndProvider']['providerName'] == 'test-service-1' &&
                it['validationReports'][0]['consumerAndProvider']['providerVersion'] == '1.0' &&
                it['validationReports'][0]['consumerAndProvider']['consumerName'] == 'test-service-2' &&
                it['validationReports'][0]['consumerAndProvider']['consumerVersion'] == '2.0' &&
                it['validationReports'][0]['interactions'][0]['validationResult'] == 'FAILED' &&
                it['validationReports'][0]['interactions'][0]['communicationInterface'] == 'rest' &&
                it['validationReports'][0]['interactions'][0]['interactionName'] == 'a request for details' &&
                it['validationReports'][0]['interactions'][0]['errors'][0] == '[Path \'/date\'] String "2018-03-15" is invalid against requested date format(s) [yyyy-MM-dd\'T\'HH:mm:ssZ, yyyy-MM-dd\'T\'HH:mm:ss.[0-9]{1,12}Z]'
            }
            response.any {
                it['service']['name'] == 'test-service-2' &&
                it['service']['version'] == '2.0' &&
                it['validationReports'][0]['consumerAndProvider']['providerName'] == 'test-service-1' &&
                it['validationReports'][0]['consumerAndProvider']['providerVersion'] == '1.0' &&
                it['validationReports'][0]['consumerAndProvider']['consumerName'] == 'test-service-2' &&
                it['validationReports'][0]['consumerAndProvider']['consumerVersion'] == '2.0' &&
                it['validationReports'][0]['interactions'][0]['validationResult'] == 'FAILED' &&
                it['validationReports'][0]['interactions'][0]['communicationInterface'] == 'rest' &&
                it['validationReports'][0]['interactions'][0]['interactionName'] == 'a request for details' &&
                it['validationReports'][0]['interactions'][0]['errors'][0] == '[Path \'/date\'] String "2018-03-15" is invalid against requested date format(s) [yyyy-MM-dd\'T\'HH:mm:ssZ, yyyy-MM-dd\'T\'HH:mm:ss.[0-9]{1,12}Z]'
            }
    }

    def "should return service validation result for environments"() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get('/environment-compatibility-report/test-service-1:1.0?environment=TEST1&environment=TEST2')
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.any {
                it['consumerAndProvider']['providerName'] == 'test-service-1' &&
                it['consumerAndProvider']['providerVersion'] == '1.0' &&
                it['consumerAndProvider']['consumerName'] == 'test-service-2' &&
                it['consumerAndProvider']['consumerVersion'] == '2.0' &&
                it['interactions'][0]['validationResult'] == 'FAILED' &&
                it['interactions'][0]['communicationInterface'] == 'rest' &&
                it['interactions'][0]['interactionName'] == 'a request for details' &&
                it['interactions'][0]['errors'][0] == '[Path \'/date\'] String "2018-03-15" is invalid against requested date format(s) [yyyy-MM-dd\'T\'HH:mm:ssZ, yyyy-MM-dd\'T\'HH:mm:ss.[0-9]{1,12}Z]'
            }
            response.any {
                it['consumerAndProvider']['providerName'] == 'test-service-1' &&
                it['consumerAndProvider']['providerVersion'] == '1.0' &&
                it['consumerAndProvider']['consumerName'] == 'test-service-2' &&
                it['consumerAndProvider']['consumerVersion'] == '3.0' &&
                it['interactions'][0]['validationResult'] == 'OK' &&
                it['interactions'][0]['communicationInterface'] == 'rest' &&
                it['interactions'][0]['interactionName'] == 'a request for details' &&
                it['interactions'][0]['errors'].size() == 0
            }
    }

    def "should return 404 if service doesn't exist"() {
        expect:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get('/environment-compatibility-report/not_exists-1:1.0?environment=TEST1&environment=TEST2')
                .then()
                .statusCode(404)
    }
}

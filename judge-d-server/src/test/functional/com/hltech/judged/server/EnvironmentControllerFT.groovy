package com.hltech.judged.server

import io.restassured.RestAssured
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql

@FunctionalTest
class EnvironmentControllerFT extends PostgresDatabaseSpecification {

    @LocalServerPort
    int serverPort

    def "should update environment"() {
        given:
            def environmentName = 'TEST'

        when:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .body("""[
                    {
                        "name": "test1",
                        "version": "1.0"
                    },
                    {
                        "name": "test2",
                        "version": "1.3"
                    }
                ]""")
                .when()
                .put("/environments/${environmentName}")
                .then()
                .statusCode(200)

        then:
            dbHelper.fetchEnvironments()[0]['name'] == environmentName
            def serviceVersions = dbHelper.fetchServiceVersions()
            serviceVersions.any {
                it['name'] == 'test1' &&
                it['version'] == "1.0" &&
                it['environment_name'] == environmentName &&
                it['space'] == 'default'
            }
            serviceVersions.any {
                it['name'] == 'test2' &&
                it['version'] == "1.3" &&
                it['environment_name'] == environmentName &&
                it['space'] == 'default'
            }
    }

    def "should update environment and replace namespace"() {
        given:
            def environmentName = 'TEST'
            def space = 'testSpace'

        when:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .header('X-JUDGE-D-AGENT-SPACE', space)
                .body("""[
                    {
                        "name": "test-service-1",
                        "version": "1.0"
                    },
                    {
                        "name": "test-service-2",
                        "version": "1.3"
                    }
                ]""")
                .when()
                .put("/environments/${environmentName}")
                .then()
                .statusCode(200)

        then:
            dbHelper.fetchEnvironments()[0]['name'] == environmentName
            def serviceVersions = dbHelper.fetchServiceVersions()
            serviceVersions.any {
                it['name'] == 'test-service-1' &&
                it['version'] == "1.0" &&
                it['environment_name'] == environmentName &&
                it['space'] == space
            }
            serviceVersions.any {
                it['name'] == 'test-service-2' &&
                it['version'] == "1.3" &&
                it['environment_name'] == environmentName &&
                it['space'] == space
            }
    }

    @Sql("EnvironmentControllerFT.sql")
    def "should update service version and replace namespace for one service"() {
        given:
            def environmentName = 'TEST1'
            def space = 'testSpace'

        when:
            RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .header('X-JUDGE-D-AGENT-SPACE', space)
                .body("""[
                    {
                        "name": "test-service-1",
                        "version": "3.0"
                    }
                ]""")
                .when()
                .put("/environments/${environmentName}")
                .then()
                .statusCode(200)

        then:
            dbHelper.fetchEnvironments()[0]['name'] == environmentName
            def serviceVersions = dbHelper.fetchServiceVersions().findAll {
                it['environment_name'] == environmentName
            }
            serviceVersions.any {
                it['name'] == 'test-service-1' &&
                it['version'] == "3.0" &&
                it['environment_name'] == environmentName &&
                it['space'] == space
            }
            serviceVersions.any {
                it['name'] == 'test-service-2' &&
                it['version'] == "2.0" &&
                it['environment_name'] == environmentName &&
                it['space'] == 'default'
            }
    }

    @Sql("EnvironmentControllerFT.sql")
    def "should get all environments"() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/environments")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.containsAll("TEST1", "TEST2")
    }

    def "should return empty list if there are no environments"() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/environments")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.size() == 0
    }

    @Sql("EnvironmentControllerFT.sql")
    def "should get service names and versions for selected environment"() {
        given:
            def environmentName = 'TEST1'
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/environments/${environmentName}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.any{
                it['name'] == "test-service-1" &&
                it['version'] == "1.0"
            }
            response.any{
                it['name'] == "test-service-2" &&
                it['version'] == "2.0"
            }
    }

    @Sql("EnvironmentControllerFT.sql")
    def "should return empty list when there are no services for selected environment"() {
        when:
            def response = RestAssured.given()
                .port(serverPort)
                .contentType("application/json")
                .when()
                .get("/environments/TEST3")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().jsonPath().getList('$')

        then:
            response.size() == 0
    }
}

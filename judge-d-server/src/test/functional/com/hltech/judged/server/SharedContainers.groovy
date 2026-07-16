package com.hltech.judged.server

import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Aggregates docker containers for Testcontainers so they are not duplicated across the tests.
 * Containers are started on class load so any test that touches this holder gets a running instance.
 */
final class SharedContainers {

    static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer(DockerImageName.parse(
            "529219089249.dkr.ecr.eu-west-1.amazonaws.com/docker-hub/library/postgres:16-alpine")
            .asCompatibleSubstituteFor("postgres"))

    static {
        POSTGRES.start()
    }

    private SharedContainers() {}
}

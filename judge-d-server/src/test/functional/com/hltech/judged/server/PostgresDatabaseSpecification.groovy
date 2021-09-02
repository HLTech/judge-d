package com.hltech.judged.server

import groovy.sql.Sql
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

abstract class PostgresDatabaseSpecification extends Specification {

    private static final Object lock = new Object()

    static PostgreSQLContainer postgres

    static Sql sql

    static DbHelper dbHelper

    def setupSpec() {
        synchronized (lock) {
            if (postgres == null) {
                postgres = new PostgreSQLContainer()
                postgres.start()
                connect()
                dbHelper = new DbHelper(sql)
            }
        }
    }

    def cleanup() {
        dbHelper.clearTables()
    }

    static void connect() {
        sql = Sql.newInstance(postgres.jdbcUrl, postgres.username, postgres.password, postgres.driverClassName)
    }
}

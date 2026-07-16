package com.hltech.judged.server

import groovy.sql.Sql
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

abstract class PostgresDatabaseSpecification extends Specification {

    private static final Object lock = new Object()

    static Sql sql

    static DbHelper dbHelper

    def setupSpec() {
        synchronized (lock) {
            if (sql == null) {
                connect()
                dbHelper = new DbHelper(sql)
            }
        }
    }

    def cleanup() {
        dbHelper.clearTables()
    }

    static void connect() {
        PostgreSQLContainer postgres = SharedContainers.POSTGRES
        sql = Sql.newInstance(postgres.jdbcUrl, postgres.username, postgres.password, postgres.driverClassName)
    }
}

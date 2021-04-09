package com.hltech.judged.server

import groovy.sql.Sql

import java.util.concurrent.TimeUnit
import static org.awaitility.Awaitility.await

class DbHelper {

    static ALL_TABLE_NAMES = ['capabilities', 'databasechangelog', 'databasechangeloglock', 'environments',
                              'expectations', 'service_contracts', 'service_versions']

    Sql sql

    DbHelper(Sql sql) {
        this.sql = sql
    }

    def fetchCapabilities() {
        sql.rows("select * from capabilities" as String)
    }

    def fetchExpectations() {
        sql.rows("select * from expectations" as String)
    }

    def fetchServiceContracts() {
        sql.rows("select * from service_contracts" as String)
    }

    def fetchServiceVersions() {
        sql.rows("select * from service_versions" as String)
    }

    def fetchEnvironments() {
        sql.rows("select * from environments" as String)
    }

    def clearTables() {
        ALL_TABLE_NAMES.each {tableName ->
            await().atMost(10, TimeUnit.SECONDS).until({
                sql.execute("delete from $tableName" as String)
                sql.rows("select * from $tableName" as String).size() == 0
            })
        }
    }
}

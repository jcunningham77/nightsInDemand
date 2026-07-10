package com.eventdemand.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:./eventdemand.db"
        Database.connect(databaseUrl, driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(EventsTable, PricesTable)
        }
    }
}

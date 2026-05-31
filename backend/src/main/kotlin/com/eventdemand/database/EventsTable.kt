package com.eventdemand.database

import org.jetbrains.exposed.sql.Table

object EventsTable : Table("events") {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val date = varchar("date", 20)
    val city = varchar("city", 100)
    val venue = varchar("venue", 255)
    val league = varchar("league", 50).nullable()
    val category = varchar("category", 50)
    val attendance = integer("estimated_attendance").nullable()
    val dataSource = varchar("source", 50)
    val cachedAt = long("cached_at")
    override val primaryKey = PrimaryKey(id)
}

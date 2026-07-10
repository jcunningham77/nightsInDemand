package com.eventdemand.database

import org.jetbrains.exposed.sql.Table

object PricesTable : Table("secondary_prices") {
    val eventId = varchar("event_id", 255)
    val dataSource = varchar("source", 50)
    val minPrice = double("min_price").nullable()
    val maxPrice = double("max_price").nullable()
    val currency = varchar("currency", 10).default("USD")
    val url = varchar("url", 500).nullable()
    val available = bool("available").default(true)
    val fetchedAt = long("fetched_at")
    override val primaryKey = PrimaryKey(eventId, dataSource)
}

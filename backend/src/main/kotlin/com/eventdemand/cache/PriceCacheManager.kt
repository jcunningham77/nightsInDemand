package com.eventdemand.cache

import com.eventdemand.database.PricesTable
import com.eventdemand.models.PriceQuote
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PriceCacheManager {

    private val ttlMillis: Long = (System.getProperty("PRICE_CACHE_TTL_MINUTES")?.toLongOrNull()
        ?: System.getenv("PRICE_CACHE_TTL_MINUTES")?.toLongOrNull()
        ?: 60L) * 60 * 1000

    /** Returns cached price quotes for an event if fresh, null if missing or stale. */
    fun getCached(eventId: String): List<PriceQuote>? = transaction {
        val rows = PricesTable.selectAll()
            .where { PricesTable.eventId eq eventId }
            .toList()

        if (rows.isEmpty()) return@transaction null

        val fetchedAt = rows.first()[PricesTable.fetchedAt]
        if (System.currentTimeMillis() - fetchedAt > ttlMillis) return@transaction null

        rows.map { it.toPriceQuote() }
    }

    /** Stores price quotes for an event, replacing any existing rows for that event. */
    fun cache(eventId: String, quotes: List<PriceQuote>) = transaction {
        PricesTable.deleteWhere { PricesTable.eventId eq eventId }

        val now = System.currentTimeMillis()
        quotes.forEach { quote ->
            PricesTable.insertIgnore {
                it[PricesTable.eventId] = eventId
                it[dataSource] = quote.source
                it[minPrice] = quote.minPrice
                it[maxPrice] = quote.maxPrice
                it[currency] = quote.currency
                it[url] = quote.url
                it[available] = quote.available
                it[fetchedAt] = now
            }
        }
    }

    private fun ResultRow.toPriceQuote() = PriceQuote(
        source = this[PricesTable.dataSource],
        minPrice = this[PricesTable.minPrice],
        maxPrice = this[PricesTable.maxPrice],
        currency = this[PricesTable.currency],
        url = this[PricesTable.url],
        available = this[PricesTable.available]
    )
}

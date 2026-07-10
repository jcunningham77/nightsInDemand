package com.eventdemand.cache

import com.eventdemand.database.EventsTable
import com.eventdemand.models.Event
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CacheManager {

    private val ttlMillis: Long = (System.getProperty("CACHE_TTL_HOURS")?.toLongOrNull()
        ?: System.getenv("CACHE_TTL_HOURS")?.toLongOrNull()
        ?: 6L) * 60 * 60 * 1000

    /** Returns cached events for a city+date if fresh, null if missing or stale. */
    fun getCachedDay(city: String, date: String): List<Event>? = transaction {
        val rows = EventsTable.selectAll()
            .where { (EventsTable.city eq city) and (EventsTable.date eq date) }
            .toList()

        if (rows.isEmpty()) return@transaction null

        val cachedAt = rows.first()[EventsTable.cachedAt]
        if (System.currentTimeMillis() - cachedAt > ttlMillis) return@transaction null

        rows.map { it.toEvent() }
    }

    /** Stores a day's events for a city, replacing any existing rows for that city+date. */
    fun cacheDay(city: String, date: String, events: List<Event>) = transaction {
        // Delete stale rows for this city+date before inserting fresh ones
        EventsTable.deleteWhere { (EventsTable.city eq city) and (EventsTable.date eq date) }

        val now = System.currentTimeMillis()
        events.forEach { event ->
            EventsTable.insertIgnore {
                it[id] = event.id
                it[name] = event.name
                it[EventsTable.date] = event.date
                it[EventsTable.city] = city
                it[venue] = event.venue
                it[league] = event.league
                it[category] = event.category
                it[attendance] = event.estimatedAttendance
                it[dataSource] = event.source
                it[minPrice] = event.minPrice
                it[maxPrice] = event.maxPrice
                it[priceCurrency] = event.priceCurrency
                it[cachedAt] = now
            }
        }
    }

    /** Deletes all cached events for a city. */
    fun invalidateCity(city: String) = transaction {
        EventsTable.deleteWhere { EventsTable.city eq city }
    }

    /** Looks up a single cached event by ID, regardless of TTL freshness. */
    fun findEventById(eventId: String): Event? = transaction {
        EventsTable.selectAll()
            .where { EventsTable.id eq eventId }
            .firstOrNull()
            ?.toEvent()
    }

    private fun ResultRow.toEvent() = Event(
        id = this[EventsTable.id],
        name = this[EventsTable.name],
        date = this[EventsTable.date],
        city = this[EventsTable.city],
        venue = this[EventsTable.venue],
        league = this[EventsTable.league],
        category = this[EventsTable.category],
        estimatedAttendance = this[EventsTable.attendance],
        source = this[EventsTable.dataSource],
        minPrice = this[EventsTable.minPrice],
        maxPrice = this[EventsTable.maxPrice],
        priceCurrency = this[EventsTable.priceCurrency]
    )
}

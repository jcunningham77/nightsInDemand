package com.eventdemand.services

import com.eventdemand.cache.CacheManager
import com.eventdemand.models.CityNightReport
import com.eventdemand.models.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AggregatorService(
    private val espnService: EspnService,
    private val ticketmasterService: TicketmasterService,
    private val cacheManager: CacheManager
) {

    private val isoDateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun buildReports(city: String, from: String, to: String): List<CityNightReport> {
        val allEvents = mutableListOf<Event>()
        val dates = datesBetween(from, to)

        // Identify which days need a fresh API fetch
        val cachedByDate = mutableMapOf<String, List<Event>>()
        val staleDates = mutableListOf<String>()

        for (date in dates) {
            val cached = cacheManager.getCachedDay(city, date)
            if (cached != null) {
                cachedByDate[date] = cached
            } else {
                staleDates.add(date)
            }
        }

        // Fetch stale days from APIs in parallel, grouped into contiguous ranges
        // to minimise the number of API calls (ESPN accepts a date range)
        if (staleDates.isNotEmpty()) {
            val freshEvents = fetchFromApis(city, staleDates.first(), staleDates.last())
                .distinctBy { it.id }

            // Cache each day's results individually (including days with zero events)
            val freshByDate = freshEvents.groupBy { it.date }
            for (date in staleDates) {
                val dayEvents = freshByDate[date] ?: emptyList()
                cacheManager.cacheDay(city, date, dayEvents)
            }

            allEvents.addAll(freshEvents)
        }

        // Merge cached + fresh
        cachedByDate.values.forEach { allEvents.addAll(it) }

        return allEvents
            .groupBy { it.date }
            .map { (date, nightEvents) ->
                val significantEvents = nightEvents.filter { it.isSignificant() }
                val score = computeDemandScore(significantEvents)
                CityNightReport(
                    date = date,
                    city = city,
                    events = nightEvents,               // full list shown in UI
                    eventCount = significantEvents.size, // only significant events count toward threshold
                    demandScore = score,
                    demandLabel = demandLabel(score)
                )
            }
            .sortedBy { it.date }
    }

    private suspend fun fetchFromApis(city: String, from: String, to: String): List<Event> = coroutineScope {
        val espn = async { espnService.fetchEvents(city, from, to) }
        val tickets = async { ticketmasterService.fetchEvents(city, from, to) }
        (espn.await() + tickets.await()).distinctBy { it.id }
    }

    fun invalidateCity(city: String) = cacheManager.invalidateCity(city)

    private fun computeDemandScore(events: List<Event>): Int {
        var score = 0
        score += events.size * 2
        score += events.sumOf { event ->
            when {
                (event.estimatedAttendance ?: 0) > 50_000 -> 3
                (event.estimatedAttendance ?: 0) > 20_000 -> 2
                else -> 1
            }.toInt()
        }
        return score.coerceIn(1, 10)
    }

    // Sports events always count. Concerts only count if venue holds 10k+.
    private fun Event.isSignificant(): Boolean = when (category) {
        "concert" -> (estimatedAttendance ?: 0) >= 10_000
        else      -> true
    }

    private fun demandLabel(score: Int) = when {
        score >= 8 -> "EXTREME"
        score >= 6 -> "HIGH"
        score >= 3 -> "MEDIUM"
        else -> "LOW"
    }

    private fun datesBetween(from: String, to: String): List<String> {
        val start = LocalDate.parse(from, isoDateFmt)
        val end = LocalDate.parse(to, isoDateFmt)
        val dates = mutableListOf<String>()
        var current = start
        while (!current.isAfter(end)) {
            dates.add(current.format(isoDateFmt))
            current = current.plusDays(1)
        }
        return dates
    }
}

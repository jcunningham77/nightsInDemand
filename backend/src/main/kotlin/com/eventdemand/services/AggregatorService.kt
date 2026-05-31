package com.eventdemand.services

import com.eventdemand.models.CityNightReport
import com.eventdemand.models.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AggregatorService(
    private val espnService: EspnService,
    private val ticketmasterService: TicketmasterService
) {

    suspend fun fetchAll(city: String, from: String, to: String): List<Event> = coroutineScope {
        val espn = async { espnService.fetchEvents(city, from, to) }
        val tickets = async { ticketmasterService.fetchEvents(city, from, to) }
        (espn.await() + tickets.await()).distinctBy { it.id }
    }

    suspend fun buildReports(city: String, from: String, to: String): List<CityNightReport> {
        val events = fetchAll(city, from, to)
        return events
            .groupBy { it.date }
            .map { (date, nightEvents) ->
                val score = computeDemandScore(nightEvents)
                CityNightReport(
                    date = date,
                    city = city,
                    events = nightEvents,
                    eventCount = nightEvents.size,
                    demandScore = score,
                    demandLabel = demandLabel(score)
                )
            }
            .sortedBy { it.date }
    }

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

    private fun demandLabel(score: Int) = when {
        score >= 8 -> "EXTREME"
        score >= 6 -> "HIGH"
        score >= 3 -> "MEDIUM"
        else -> "LOW"
    }
}

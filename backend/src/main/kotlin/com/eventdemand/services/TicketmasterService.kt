package com.eventdemand.services

import com.eventdemand.models.Event
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

class TicketmasterService(private val client: HttpClient) {

    private val apiKey get() = System.getenv("TICKETMASTER_API_KEY")
        ?: System.getProperty("TICKETMASTER_API_KEY")
        ?: ""

    // Capacity estimates for well-known venues (Ticketmaster doesn't expose capacity)
    private val venueCapacities = mapOf(
        "madison square garden" to 20000,
        "metlife stadium" to 82500,
        "yankee stadium" to 47000,
        "citi field" to 41922,
        "barclays center" to 19000,
        "united center" to 20000,
        "crypto.com arena" to 20000,
        "sofi stadium" to 70240,
        "at&t stadium" to 80000,
        "allegiant stadium" to 65000,
        "wrigley field" to 41649,
        "fenway park" to 37755,
    )

    suspend fun fetchEvents(city: String, from: String, to: String): List<Event> {
        if (apiKey.isBlank()) {
            System.err.println("TICKETMASTER_API_KEY not set — skipping Ticketmaster fetch")
            return emptyList()
        }

        val results = mutableListOf<Event>()
        var page = 0
        val pageSize = 200

        try {
            do {
                val url = buildUrl(city, from, to, page, pageSize)
                val responseText: String = client.get(url).body()
                val json = Json { ignoreUnknownKeys = true }
                val root = json.parseToJsonElement(responseText).jsonObject

                val events = root["_embedded"]?.jsonObject?.get("events")?.jsonArray ?: break
                results.addAll(events.mapNotNull { parseEvent(it.jsonObject, city) }
                    .filter { it.date >= from && it.date <= to })

                val totalPages = root["page"]?.jsonObject?.get("totalPages")?.jsonPrimitive?.int ?: 1
                page++
                if (page >= totalPages || page >= 5) break  // cap at 5 pages / 1000 events
            } while (true)
        } catch (e: Exception) {
            System.err.println("Ticketmaster fetch failed: ${e.message}")
        }

        return results
    }

    private fun buildUrl(city: String, from: String, to: String, page: Int, size: Int): String {
        val startDt = "${from}T00:00:00Z"
        val endDt = "${to}T23:59:59Z"
        return "https://app.ticketmaster.com/discovery/v2/events.json" +
            "?city=${city.encodeUrl()}" +
            "&classificationName=music,sports" +
            "&startDateTime=$startDt" +
            "&endDateTime=$endDt" +
            "&size=$size" +
            "&page=$page" +
            "&apikey=$apiKey"
    }

    private fun parseEvent(event: JsonObject, requestedCity: String): Event? {
        return try {
            val id = "tm-${event["id"]?.jsonPrimitive?.content ?: return null}"
            val name = event["name"]?.jsonPrimitive?.content ?: return null
            val date = event["dates"]?.jsonObject
                ?.get("start")?.jsonObject
                ?.get("localDate")?.jsonPrimitive?.content ?: return null

            val venues = event["_embedded"]?.jsonObject?.get("venues")?.jsonArray
            val venue = venues?.firstOrNull()?.jsonObject
            val venueName = venue?.get("name")?.jsonPrimitive?.content ?: "Unknown Venue"
            val venueCity = venue?.get("city")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: requestedCity

            val classification = event["classifications"]?.jsonArray?.firstOrNull()?.jsonObject
            val segment = classification?.get("segment")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Other"
            val category = when (segment.lowercase()) {
                "music" -> "concert"
                "sports" -> "sports"
                else -> "other"
            }

            val estimatedAttendance = venueCapacities[venueName.lowercase()]

            Event(
                id = id,
                name = name,
                date = date,
                city = venueCity,
                venue = venueName,
                league = null,
                category = category,
                estimatedAttendance = estimatedAttendance,
                source = "ticketmaster"
            )
        } catch (e: Exception) {
            System.err.println("Failed to parse Ticketmaster event: ${e.message}")
            null
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}

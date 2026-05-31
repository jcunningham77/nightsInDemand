package com.eventdemand.services

import com.eventdemand.models.Event
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EspnService(private val client: HttpClient) {

    private val espnDateFmt = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val isoDateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    // Map of league slug -> (sport path, league path, league label)
    private val leagues = mapOf(
        "mlb"  to Triple("baseball",    "mlb",    "MLB"),
        "nba"  to Triple("basketball",  "nba",    "NBA"),
        "nfl"  to Triple("football",    "nfl",    "NFL"),
        "nhl"  to Triple("hockey",      "nhl",    "NHL"),
        "mls"  to Triple("soccer",      "usa.1",  "MLS")
    )

    suspend fun fetchEvents(city: String, from: String, to: String): List<Event> {
        val results = mutableListOf<Event>()
        val dates = datesBetween(from, to)

        for ((leagueKey, info) in leagues) {
            val (sport, league, label) = info
            for (date in dates) {
                try {
                    val dateParam = LocalDate.parse(date, isoDateFmt).format(espnDateFmt)
                    val events = fetchLeagueDay(sport, league, label, city, date, dateParam)
                    results.addAll(events)
                } catch (e: Exception) {
                    // Best-effort: log and continue if ESPN is down for a given day
                    System.err.println("ESPN $leagueKey fetch failed for $date: ${e.message}")
                }
            }
        }
        return results
    }

    private suspend fun fetchLeagueDay(
        sport: String,
        league: String,
        leagueLabel: String,
        city: String,
        isoDate: String,
        dateParam: String
    ): List<Event> {
        val url = "https://site.api.espn.com/apis/site/v2/sports/$sport/$league/scoreboard?dates=$dateParam"
        val responseText: String = client.get(url).body()
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(responseText).jsonObject

        val events = root["events"]?.jsonArray ?: return emptyList()

        return events.mapNotNull { eventEl ->
            try {
                val event = eventEl.jsonObject
                val competition = event["competitions"]?.jsonArray?.firstOrNull()?.jsonObject ?: return@mapNotNull null
                val venueObj = competition["venue"]?.jsonObject
                // MLS returns "Seattle, Washington" — take only the city part before the comma
                val rawCity = venueObj?.get("address")?.jsonObject?.get("city")?.jsonPrimitive?.content ?: ""
                val venueCity = rawCity.substringBefore(",").trim()

                // Filter to requested city, with NYC borough aliases
                val cityAliases = nycAliases(city)
                if (cityAliases.none { venueCity.contains(it, ignoreCase = true) }) return@mapNotNull null

                val venueName = venueObj?.get("fullName")?.jsonPrimitive?.content ?: "Unknown Venue"
                val attendance = competition["attendance"]?.jsonPrimitive?.intOrNull

                val competitors = competition["competitors"]?.jsonArray ?: JsonArray(emptyList())
                val home = competitors.firstOrNull { it.jsonObject["homeAway"]?.jsonPrimitive?.content == "home" }
                val away = competitors.firstOrNull { it.jsonObject["homeAway"]?.jsonPrimitive?.content == "away" }
                val homeName = home?.jsonObject?.get("team")?.jsonObject?.get("displayName")?.jsonPrimitive?.content ?: ""
                val awayName = away?.jsonObject?.get("team")?.jsonObject?.get("displayName")?.jsonPrimitive?.content ?: ""

                val eventId = "espn-${league}-${event["id"]?.jsonPrimitive?.content}"
                val eventName = "$awayName @ $homeName"

                Event(
                    id = eventId,
                    name = eventName,
                    date = isoDate,
                    city = venueCity,
                    venue = venueName,
                    league = leagueLabel,
                    category = "sports",
                    estimatedAttendance = attendance,
                    source = "espn"
                )
            } catch (e: Exception) {
                System.err.println("Failed to parse ESPN event: ${e.message}")
                null
            }
        }
    }

    private fun nycAliases(city: String): List<String> {
        val nyc = listOf("New York", "Bronx", "Brooklyn", "Queens", "Manhattan", "Flushing", "East Rutherford")
        return if (nyc.any { it.equals(city, ignoreCase = true) || city.contains(it, ignoreCase = true) }) nyc
        else listOf(city)
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

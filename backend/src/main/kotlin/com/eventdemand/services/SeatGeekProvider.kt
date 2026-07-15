package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

class SeatGeekProvider(private val client: HttpClient) : SecondaryMarketProvider {

    override val sourceName = "seatgeek"

    private val clientId get() = System.getenv("SEATGEEK_CLIENT_ID")
        ?: System.getProperty("SEATGEEK_CLIENT_ID")
        ?: ""

    override suspend fun findPrice(event: Event): PriceQuote {
        if (clientId.isBlank()) {
            System.err.println("SEATGEEK_CLIENT_ID not set — skipping SeatGeek lookup")
            return PriceQuote(source = sourceName, available = false)
        }

        return try {
            val url = "https://api.seatgeek.com/2/events" +
                "?client_id=$clientId" +
                "&q=${event.name.encodeUrl()}" +
                "&venue.city=${event.city.encodeUrl()}" +
                "&datetime_local.gte=${event.date}T00:00:00" +
                "&datetime_local.lte=${event.date}T23:59:59"

            val response = client.get(url)
            val responseText: String = response.body()
            if (response.status.value != 200) {
                System.err.println("SeatGeek: HTTP ${response.status.value} for '${event.name}': $responseText")
                return PriceQuote(source = sourceName, available = false)
            }

            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseText).jsonObject
            val events = root["events"]?.jsonArray ?: JsonArray(emptyList())
            // Prefer an exact title match — a fuzzy `q` search can surface tribute acts
            // or unrelated events (e.g. "Weird Phishes") ahead of the real event.
            val match = events.map { it.jsonObject }.firstOrNull {
                it["title"]?.jsonPrimitive?.content?.equals(event.name, ignoreCase = true) == true
            } ?: events.firstOrNull()?.jsonObject

            if (match == null) {
                System.err.println("SeatGeek: no match for '${event.name}' in ${event.city} on ${event.date}")
                return PriceQuote(source = sourceName, available = false)
            }

            val stats = match["stats"]?.jsonObject
            val minPrice = stats?.get("lowest_price")?.jsonPrimitive?.doubleOrNull
            val maxPrice = stats?.get("highest_price")?.jsonPrimitive?.doubleOrNull
            val eventUrl = match["url"]?.jsonPrimitive?.content

            // Even when SeatGeek doesn't return price stats for this account tier, a matched
            // event still gives us a real deep link the user can check prices on manually.
            PriceQuote(
                source = sourceName,
                minPrice = minPrice,
                maxPrice = maxPrice,
                url = eventUrl,
                available = minPrice != null || maxPrice != null
            )
        } catch (e: Exception) {
            System.err.println("SeatGeek lookup failed: ${e.message}")
            PriceQuote(source = sourceName, available = false)
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}

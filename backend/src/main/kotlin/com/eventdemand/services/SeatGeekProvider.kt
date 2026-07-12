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
                "&datetime_local.date=${event.date}"

            val responseText: String = client.get(url).body()
            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseText).jsonObject
            val match = root["events"]?.jsonArray?.firstOrNull()?.jsonObject
            if (match == null) {
                System.err.println("SeatGeek: no match for '${event.name}' in ${event.city} on ${event.date}")
                return PriceQuote(source = sourceName, available = false)
            }

            val stats = match["stats"]?.jsonObject
            val minPrice = stats?.get("lowest_price")?.jsonPrimitive?.doubleOrNull
            val maxPrice = stats?.get("highest_price")?.jsonPrimitive?.doubleOrNull
            val eventUrl = match["url"]?.jsonPrimitive?.content

            if (minPrice == null && maxPrice == null) {
                PriceQuote(source = sourceName, available = false)
            } else {
                PriceQuote(
                    source = sourceName,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    url = eventUrl,
                    available = true
                )
            }
        } catch (e: Exception) {
            System.err.println("SeatGeek lookup failed: ${e.message}")
            PriceQuote(source = sourceName, available = false)
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}

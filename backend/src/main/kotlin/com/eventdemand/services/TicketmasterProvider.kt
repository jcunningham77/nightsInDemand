package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

/**
 * Live Ticketmaster price lookup for events that didn't originate from TicketmasterService
 * (e.g. ESPN-sourced sports games) — searches Discovery API by name/city/date instead of
 * relying on price data embedded at the event's original fetch time.
 */
class TicketmasterProvider(private val client: HttpClient) : SecondaryMarketProvider {

    override val sourceName = "ticketmaster"

    private val apiKey get() = System.getenv("TICKETMASTER_API_KEY")
        ?: System.getProperty("TICKETMASTER_API_KEY")
        ?: ""

    override suspend fun findPrice(event: Event): PriceQuote {
        if (apiKey.isBlank()) {
            System.err.println("TICKETMASTER_API_KEY not set — skipping Ticketmaster live lookup")
            return PriceQuote(source = sourceName, available = false)
        }

        return try {
            val url = "https://app.ticketmaster.com/discovery/v2/events.json" +
                "?keyword=${event.name.encodeUrl()}" +
                "&city=${event.city.encodeUrl()}" +
                "&startDateTime=${event.date}T00:00:00Z" +
                "&endDateTime=${event.date}T23:59:59Z" +
                "&size=1" +
                "&apikey=$apiKey"

            val responseText: String = client.get(url).body()
            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseText).jsonObject
            val match = root["_embedded"]?.jsonObject?.get("events")?.jsonArray?.firstOrNull()?.jsonObject
            if (match == null) {
                System.err.println("Ticketmaster: no match for '${event.name}' in ${event.city} on ${event.date}")
                return PriceQuote(source = sourceName, available = false)
            }

            val priceRange = match["priceRanges"]?.jsonArray?.firstOrNull()?.jsonObject
            val minPrice = priceRange?.get("min")?.jsonPrimitive?.doubleOrNull
            val maxPrice = priceRange?.get("max")?.jsonPrimitive?.doubleOrNull
            val currency = priceRange?.get("currency")?.jsonPrimitive?.content ?: "USD"
            val eventUrl = match["url"]?.jsonPrimitive?.content

            if (minPrice == null && maxPrice == null) {
                PriceQuote(source = sourceName, available = false)
            } else {
                PriceQuote(
                    source = sourceName,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    currency = currency,
                    url = eventUrl,
                    available = true
                )
            }
        } catch (e: Exception) {
            System.err.println("Ticketmaster live lookup failed: ${e.message}")
            PriceQuote(source = sourceName, available = false)
        }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}

package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

/**
 * Live Ticketmaster price lookup for events that didn't originate from TicketmasterService
 * (e.g. ESPN-sourced sports games) — searches Discovery API by venue/date instead of relying
 * on price data embedded at the event's original fetch time.
 *
 * Searches by venue name rather than city: Ticketmaster's own venue records use the
 * neighborhood/borough as "city" (e.g. Citi Field is registered under "Flushing", not
 * "New York"), so filtering by our app's city value silently excludes real matches. Venue
 * name is far more precise anyway — a `city` search returns everything in a metro area,
 * while a `keyword` search on the venue name plus a one-day window reliably narrows to the
 * single event we're looking for.
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
            val match = search(event.venue, event.date) ?: search(event.name, event.date)
            if (match == null) {
                System.err.println("Ticketmaster: no match for '${event.name}' at ${event.venue} on ${event.date}")
                return PriceQuote(source = sourceName, available = false)
            }

            val priceRange = match["priceRanges"]?.jsonArray?.firstOrNull()?.jsonObject
            val minPrice = priceRange?.get("min")?.jsonPrimitive?.doubleOrNull
            val maxPrice = priceRange?.get("max")?.jsonPrimitive?.doubleOrNull
            val currency = priceRange?.get("currency")?.jsonPrimitive?.content ?: "USD"
            val eventUrl = match["url"]?.jsonPrimitive?.content

            // Ticketmaster's own catalog sometimes has no priceRanges for an onsale event —
            // still surface the real listing URL as a fallback so the user can check manually.
            PriceQuote(
                source = sourceName,
                minPrice = minPrice,
                maxPrice = maxPrice,
                currency = currency,
                url = eventUrl,
                available = minPrice != null || maxPrice != null
            )
        } catch (e: Exception) {
            System.err.println("Ticketmaster live lookup failed: ${e.message}")
            PriceQuote(source = sourceName, available = false)
        }
    }

    private suspend fun search(keyword: String, date: String): JsonObject? {
        val url = "https://app.ticketmaster.com/discovery/v2/events.json" +
            "?keyword=${keyword.encodeUrl()}" +
            "&startDateTime=${date}T00:00:00Z" +
            "&endDateTime=${date}T23:59:59Z" +
            "&size=1" +
            "&apikey=$apiKey"

        val responseText: String = client.get(url).body()
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(responseText).jsonObject
        return root["_embedded"]?.jsonObject?.get("events")?.jsonArray?.firstOrNull()?.jsonObject
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}

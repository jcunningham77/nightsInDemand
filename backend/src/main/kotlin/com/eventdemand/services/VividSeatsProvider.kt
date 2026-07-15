package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote

/**
 * Vivid Seats does not offer a self-serve developer API — access requires a partner/affiliate
 * agreement. This provider is wired into the same interface as SeatGeek so a real
 * implementation can be dropped in once VIVIDSEATS_API_KEY credentials are obtained.
 */
class VividSeatsProvider : SecondaryMarketProvider {

    override val sourceName = "vividseats"

    private val apiKey get() = System.getenv("VIVIDSEATS_API_KEY")
        ?: System.getProperty("VIVIDSEATS_API_KEY")
        ?: ""

    override suspend fun findPrice(event: Event): PriceQuote {
        if (apiKey.isBlank()) {
            return PriceQuote(source = sourceName, available = false)
        }
        // No partner credentials configured yet — real lookup not implemented.
        return PriceQuote(source = sourceName, available = false)
    }
}

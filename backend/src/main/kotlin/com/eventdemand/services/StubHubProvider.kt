package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote

/**
 * StubHub does not offer a self-serve developer API — access requires a partner/affiliate
 * agreement. This provider is wired into the same interface as SeatGeek so a real
 * implementation can be dropped in once STUBHUB_API_KEY credentials are obtained.
 */
class StubHubProvider : SecondaryMarketProvider {

    override val sourceName = "stubhub"

    private val apiKey get() = System.getenv("STUBHUB_API_KEY")
        ?: System.getProperty("STUBHUB_API_KEY")
        ?: ""

    override suspend fun findPrice(event: Event): PriceQuote {
        if (apiKey.isBlank()) {
            return PriceQuote(source = sourceName, available = false)
        }
        // No partner credentials configured yet — real lookup not implemented.
        return PriceQuote(source = sourceName, available = false)
    }
}

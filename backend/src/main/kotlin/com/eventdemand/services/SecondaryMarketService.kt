package com.eventdemand.services

import com.eventdemand.cache.PriceCacheManager
import com.eventdemand.models.Event
import com.eventdemand.models.PriceComparison
import com.eventdemand.models.PriceQuote
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SecondaryMarketService(
    private val providers: List<SecondaryMarketProvider>,
    private val priceCacheManager: PriceCacheManager
) {

    suspend fun getComparison(event: Event): PriceComparison {
        val cached = priceCacheManager.getCached(event.id)
        val quotes = cached ?: fetchFresh(event)
        return build(event.id, quotes)
    }

    private suspend fun fetchFresh(event: Event): List<PriceQuote> = coroutineScope {
        val ticketmasterQuote = PriceQuote(
            source = "ticketmaster",
            minPrice = event.minPrice,
            maxPrice = event.maxPrice,
            currency = event.priceCurrency ?: "USD",
            available = event.minPrice != null || event.maxPrice != null
        )

        val secondaryQuotes = providers.map { provider ->
            async { provider.findPrice(event) }
        }.awaitAll()

        val quotes = listOf(ticketmasterQuote) + secondaryQuotes
        priceCacheManager.cache(event.id, quotes)
        quotes
    }

    private fun build(eventId: String, quotes: List<PriceQuote>): PriceComparison {
        val cheapest = quotes
            .filter { it.available && it.minPrice != null }
            .minByOrNull { it.minPrice!! }

        return PriceComparison(
            eventId = eventId,
            quotes = quotes,
            cheapestSource = cheapest?.source,
            fetchedAt = System.currentTimeMillis()
        )
    }
}

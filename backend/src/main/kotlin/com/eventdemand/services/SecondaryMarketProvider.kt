package com.eventdemand.services

import com.eventdemand.models.Event
import com.eventdemand.models.PriceQuote

interface SecondaryMarketProvider {
    val sourceName: String
    suspend fun findPrice(event: Event): PriceQuote
}

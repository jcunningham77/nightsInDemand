package com.eventdemand.models

import kotlinx.serialization.Serializable

@Serializable
data class PriceQuote(
    val source: String,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val currency: String = "USD",
    val url: String? = null,
    val available: Boolean = true
)

@Serializable
data class PriceComparison(
    val eventId: String,
    val quotes: List<PriceQuote>,
    val cheapestSource: String?,
    val fetchedAt: Long
)

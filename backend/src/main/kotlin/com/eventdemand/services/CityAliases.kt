package com.eventdemand.services

/**
 * Some major venues are registered under a neighborhood/suburb name rather than their
 * metro area's colloquial city name (e.g. Citi Field is tagged "Flushing", not "New York",
 * in both ESPN's and Ticketmaster's venue data). This maps a searched city to every name
 * its metro's venues might be filed under, so city-scoped searches don't silently miss them.
 */
object CityAliases {
    private val metros = listOf(
        listOf("New York", "Bronx", "Brooklyn", "Queens", "Manhattan", "Flushing", "East Rutherford")
    )

    /** Returns every name a metro's venues might be filed under, if `city` matches a known metro. */
    fun forCity(city: String): List<String> {
        val metro = metros.firstOrNull { aliases ->
            aliases.any { it.equals(city, ignoreCase = true) || city.contains(it, ignoreCase = true) }
        }
        return metro ?: listOf(city)
    }
}

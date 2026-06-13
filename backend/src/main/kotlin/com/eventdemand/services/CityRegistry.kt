package com.eventdemand.services

data class CityLocation(
    val name: String,
    val lat: Double,
    val lng: Double
)

/** Curated list of major US metros with pro sports/concert venues, for the demand heatmap. */
object CityRegistry {
    val cities = listOf(
        CityLocation("New York", 40.7128, -74.0060),
        CityLocation("Los Angeles", 34.0522, -118.2437),
        CityLocation("Chicago", 41.8781, -87.6298),
        CityLocation("Boston", 42.3601, -71.0589),
        CityLocation("Philadelphia", 39.9526, -75.1652),
        CityLocation("Dallas", 32.7767, -96.7970),
        CityLocation("Houston", 29.7601, -95.3701),
        CityLocation("Miami", 25.7617, -80.1918),
        CityLocation("Atlanta", 33.7490, -84.3880),
        CityLocation("Washington", 38.9072, -77.0369),
        CityLocation("San Francisco", 37.7749, -122.4194),
        CityLocation("Seattle", 47.6062, -122.3321),
        CityLocation("Denver", 39.7392, -104.9903),
        CityLocation("Phoenix", 33.4484, -112.0740),
        CityLocation("Detroit", 42.3314, -83.0458),
        CityLocation("Minneapolis", 44.9778, -93.2650),
        CityLocation("Cleveland", 41.4993, -81.6944),
        CityLocation("Pittsburgh", 40.4406, -79.9959),
        CityLocation("San Diego", 32.7157, -117.1611),
        CityLocation("Las Vegas", 36.1699, -115.1398),
    )
}

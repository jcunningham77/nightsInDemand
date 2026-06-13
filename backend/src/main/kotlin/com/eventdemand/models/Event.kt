package com.eventdemand.models

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val name: String,
    val date: String,
    val city: String,
    val venue: String,
    val league: String? = null,
    val category: String,
    val estimatedAttendance: Int? = null,
    val source: String
)

@Serializable
data class CityNightReport(
    val date: String,
    val city: String,
    val events: List<Event>,
    val eventCount: Int,
    val demandScore: Int,
    val demandLabel: String
)

@Serializable
data class CityDemandSummary(
    val city: String,
    val lat: Double,
    val lng: Double,
    val date: String,
    val eventCount: Int,
    val demandScore: Int,
    val demandLabel: String
)

package com.eventdemand.routes

import com.eventdemand.cache.CacheManager
import com.eventdemand.services.SecondaryMarketService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.priceRoutes(cacheManager: CacheManager, secondaryMarketService: SecondaryMarketService) {
    get("/api/prices/{eventId}") {
        val eventId = call.parameters["eventId"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "eventId required")
        val event = cacheManager.findEventById(eventId)
            ?: return@get call.respond(HttpStatusCode.NotFound, "Unknown event id: $eventId")
        val comparison = secondaryMarketService.getComparison(event)
        call.respond(comparison)
    }
}

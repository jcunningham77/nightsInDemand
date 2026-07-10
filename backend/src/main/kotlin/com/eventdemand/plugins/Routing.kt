package com.eventdemand.plugins

import com.eventdemand.cache.CacheManager
import com.eventdemand.routes.eventRoutes
import com.eventdemand.routes.priceRoutes
import com.eventdemand.services.AggregatorService
import com.eventdemand.services.SecondaryMarketService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    aggregator: AggregatorService,
    cacheManager: CacheManager,
    secondaryMarketService: SecondaryMarketService
) {
    routing {
        get("/api/health") {
            call.respond(mapOf("status" to "ok"))
        }
        eventRoutes(aggregator)
        priceRoutes(cacheManager, secondaryMarketService)
    }
}

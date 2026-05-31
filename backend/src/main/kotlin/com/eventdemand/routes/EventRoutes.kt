package com.eventdemand.routes

import com.eventdemand.services.AggregatorService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.eventRoutes(aggregator: AggregatorService) {
    route("/api/events/{city}") {
        get {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val from = call.request.queryParameters["from"] ?: return@get call.respond(HttpStatusCode.BadRequest, "from date required")
            val to = call.request.queryParameters["to"] ?: return@get call.respond(HttpStatusCode.BadRequest, "to date required")
            val reports = aggregator.buildReports(city, from, to)
            call.respond(reports)
        }

        get("/highdemand") {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val from = call.request.queryParameters["from"] ?: return@get call.respond(HttpStatusCode.BadRequest, "from date required")
            val to = call.request.queryParameters["to"] ?: return@get call.respond(HttpStatusCode.BadRequest, "to date required")
            val threshold = call.request.queryParameters["threshold"]?.toIntOrNull() ?: 2
            val reports = aggregator.buildReports(city, from, to).filter { it.eventCount >= threshold }
            call.respond(reports)
        }
    }

    delete("/api/cache/{city}") {
        val city = call.parameters["city"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "City required")
        call.respond(mapOf("message" to "Cache cleared for $city"))
    }
}

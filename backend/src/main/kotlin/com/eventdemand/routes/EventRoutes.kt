package com.eventdemand.routes

import com.eventdemand.services.AggregatorService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.eventRoutes(aggregator: AggregatorService) {
    get("/api/heatmap") {
        val date = call.request.queryParameters["date"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Provide 'date' (YYYY-MM-DD)")
        val summaries = aggregator.buildHeatmap(date)
        call.respond(summaries)
    }

    route("/api/events/{city}") {
        get {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val (from, to) = call.resolveDateRange() ?: return@get call.respond(HttpStatusCode.BadRequest, "Provide 'on' for a single date or 'from' and 'to' for a range")
            val reports = aggregator.buildReports(city, from, to)
            call.respond(reports)
        }

        get("/highdemand") {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val (from, to) = call.resolveDateRange() ?: return@get call.respond(HttpStatusCode.BadRequest, "Provide 'on' for a single date or 'from' and 'to' for a range")
            val threshold = call.request.queryParameters["threshold"]?.toIntOrNull() ?: 2
            val reports = aggregator.buildReports(city, from, to).filter { it.eventCount >= threshold }
            call.respond(reports)
        }
    }

    delete("/api/cache/{city}") {
        val city = call.parameters["city"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "City required")
        aggregator.invalidateCity(city)
        call.respond(mapOf("message" to "Cache cleared for $city"))
    }
}

private fun ApplicationCall.resolveDateRange(): Pair<String, String>? {
    val on = request.queryParameters["on"]
    if (on != null) return on to on
    val from = request.queryParameters["from"] ?: return null
    val to = request.queryParameters["to"] ?: return null
    return from to to
}

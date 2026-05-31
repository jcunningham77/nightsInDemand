package com.eventdemand.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.eventRoutes() {
    route("/api/events/{city}") {
        get {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val from = call.request.queryParameters["from"] ?: return@get call.respond(HttpStatusCode.BadRequest, "from date required")
            val to = call.request.queryParameters["to"] ?: return@get call.respond(HttpStatusCode.BadRequest, "to date required")
            call.respond(mapOf("city" to city, "from" to from, "to" to to, "events" to emptyList<String>()))
        }

        get("/highdemand") {
            val city = call.parameters["city"] ?: return@get call.respond(HttpStatusCode.BadRequest, "City required")
            val from = call.request.queryParameters["from"] ?: return@get call.respond(HttpStatusCode.BadRequest, "from date required")
            val to = call.request.queryParameters["to"] ?: return@get call.respond(HttpStatusCode.BadRequest, "to date required")
            call.respond(mapOf("city" to city, "from" to from, "to" to to, "highDemandNights" to emptyList<String>()))
        }
    }

    delete("/api/cache/{city}") {
        val city = call.parameters["city"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "City required")
        call.respond(mapOf("message" to "Cache cleared for $city"))
    }
}

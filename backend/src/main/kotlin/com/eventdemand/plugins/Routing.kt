package com.eventdemand.plugins

import com.eventdemand.routes.eventRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/api/health") {
            call.respond(mapOf("status" to "ok"))
        }
        eventRoutes()
    }
}

package com.eventdemand

import com.eventdemand.database.DatabaseFactory
import com.eventdemand.plugins.configureCORS
import com.eventdemand.plugins.configureRouting
import com.eventdemand.plugins.configureSerialization
import com.eventdemand.services.AggregatorService
import com.eventdemand.services.EspnService
import com.eventdemand.services.TicketmasterService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureCORS()

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val espnService = EspnService(httpClient)
    val ticketmasterService = TicketmasterService(httpClient)
    val aggregatorService = AggregatorService(espnService, ticketmasterService)

    configureRouting(aggregatorService)
}

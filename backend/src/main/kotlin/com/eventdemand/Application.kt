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

fun main(args: Array<String>) {
    loadDotEnv()
    EngineMain.main(args)
}

fun loadDotEnv() {
    val envFile = java.io.File(".env")
    if (!envFile.exists()) {
        println("env file not found: ${envFile.absolutePath}")
        return
    }
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("#")) return@forEachLine
        val (key, value) = trimmed.split("=", limit = 2).let { it[0].trim() to it.getOrElse(1) { "" }.trim() }
        if (System.getenv(key) == null) {
            // Only set if not already provided by the environment (Railway, etc.)
            ProcessBuilder().also { it.environment()[key] = value }
            System.setProperty(key, value)
        }
    }
}

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

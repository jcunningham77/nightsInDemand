package com.eventdemand

import com.eventdemand.database.DatabaseFactory
import com.eventdemand.plugins.configureCORS
import com.eventdemand.plugins.configureRouting
import com.eventdemand.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureCORS()
    configureRouting()
}

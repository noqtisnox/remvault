package dev.remvault

import configureDatabases
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureMonitoring()
    configureSockets()
    configureSecurity()
    configureSerialization()
    configureRouting()
}

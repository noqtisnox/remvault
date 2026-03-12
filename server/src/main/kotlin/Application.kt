package dev.remvault

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureMonitoring()
    configureSockets()
    configureSecurity()
    configureSerialization()
    configureRouting()
}

package dev.remvault

import dev.remvault.server.routing.authRoutes
import dev.remvault.server.routing.characterRoutes
import dev.remvault.server.routing.campaignRoutes
import dev.remvault.server.routing.diceRoutes
import dev.remvault.server.routing.aiRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "version" to "0.1.0"))
        }

        route("/api/v1") {
            authRoutes()
            characterRoutes()
            campaignRoutes()
            diceRoutes()
            aiRoutes()
        }
    }
}
package dev.remvault.server.routing

import dev.remvault.server.dto.RollRequest
import dev.remvault.server.dto.RollResponse
import dev.remvault.server.services.DiceService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.diceRoutes() {
    route("/dice") {

        // POST /api/v1/dice/roll
        post("/roll") {
            val req = call.receive<RollRequest>()

            try {
                val result = DiceService.rollExpression(req.expression)
                call.respond(HttpStatusCode.OK, RollResponse(
                    expression = result.expression,
                    rolls = result.rolls,
                    modifier = result.modifier,
                    total = result.total
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

    }
}
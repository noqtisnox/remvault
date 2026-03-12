package dev.remvault.server.routing

import dev.remvault.server.dto.*
import dev.remvault.server.services.CharacterService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.characterRoutes() {
    authenticate("auth-jwt") {
        route("/characters") {

            // POST /api/v1/characters
            post {
                val userId = call.userId()
                val req    = call.receive<CreateCharacterRequest>()

                if (req.name.isBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Name is required"))
                if (req.race.isBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Race is required"))
                if (req.characterClass.isBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Class is required"))
                if (req.background.isBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Background is required"))

                val sheet = CharacterService.createCharacter(
                    userId         = userId,
                    name           = req.name,
                    race           = req.race,
                    characterClass = req.characterClass,
                    background     = req.background,
                    campaignId     = req.campaignId,
                    alignment      = req.alignment,
                    subrace        = req.subrace,
                    subclass       = req.subclass,
                )
                call.respond(HttpStatusCode.Created, sheet)
            }

            // GET /api/v1/characters — all characters for current user
            get {
                val userId = call.userId()
                call.respond(CharacterService.getCharactersByUser(userId))
            }

            route("/{id}") {

                // GET /api/v1/characters/{id}
                get {
                    val id    = call.parameters["id"]!!
                    val sheet = CharacterService.getCharacter(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Character not found"))
                    call.respond(sheet)
                }

                // PATCH /api/v1/characters/{id}
                patch {
                    val id     = call.parameters["id"]!!
                    val userId = call.userId()
                    val req    = call.receive<UpdateCharacterRequest>()

                    val sheet = try {
                        CharacterService.updateCharacter(id, userId, req.name, req.alignment, req.subclass, req.experiencePoints)
                    } catch (e: NoSuchElementException) {
                        return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: IllegalAccessException) {
                        return@patch call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                    }
                    call.respond(sheet)
                }

                // PATCH /api/v1/characters/{id}/hp
                patch("/hp") {
                    val id  = call.parameters["id"]!!
                    val req = call.receive<UpdateHitPointsRequest>()

                    val hp = try {
                        CharacterService.updateHitPoints(id, req.current)
                    } catch (e: NoSuchElementException) {
                        return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    }
                    call.respond(hp)
                }

                // DELETE /api/v1/characters/{id}
                delete {
                    val id     = call.parameters["id"]!!
                    val userId = call.userId()

                    try {
                        CharacterService.deleteCharacter(id, userId)
                    } catch (e: NoSuchElementException) {
                        return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: IllegalAccessException) {
                        return@delete call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                    }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

// Extension to extract userId from JWT cleanly
fun io.ktor.server.application.ApplicationCall.userId(): String =
    principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
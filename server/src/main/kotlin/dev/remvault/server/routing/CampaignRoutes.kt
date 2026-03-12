package dev.remvault.server.routing

import dev.remvault.server.dto.*
import dev.remvault.server.services.CampaignService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.campaignRoutes() {
    authenticate("auth-jwt") {
        route("/campaigns") {

            // POST /api/v1/campaigns
            post {
                val userId = call.userId()
                val req    = call.receive<CreateCampaignRequest>()

                if (req.name.isBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Name is required"))

                val campaign = CampaignService.createCampaign(
                    masterId    = userId,
                    name        = req.name,
                    description = req.description,
                    setting     = req.setting
                )
                call.respond(HttpStatusCode.Created, campaign)
            }

            // GET /api/v1/campaigns — campaigns for current user (as master or player)
            get {
                val userId = call.userId()
                val asMaster = CampaignService.getCampaignsByMaster(userId)
                val asPlayer = CampaignService.getCampaignsByPlayer(userId)
                // Merge and deduplicate (master can also be a player in other campaigns)
                val all = (asMaster + asPlayer).distinctBy { it.id }
                call.respond(all)
            }

            route("/{campaignId}") {

                // GET /api/v1/campaigns/{campaignId}
                get {
                    val campaignId = call.parameters["campaignId"]!!
                    val campaign = try {
                        CampaignService.getCampaign(campaignId)
                    } catch (e: NoSuchElementException) {
                        return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    }
                    call.respond(campaign)
                }

                // POST /api/v1/campaigns/{campaignId}/archive
                post("/archive") {
                    val campaignId = call.parameters["campaignId"]!!
                    val userId     = call.userId()
                    val campaign = try {
                        CampaignService.archiveCampaign(campaignId, userId)
                    } catch (e: NoSuchElementException) {
                        return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: IllegalAccessException) {
                        return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                    }
                    call.respond(campaign)
                }

                // ── Members ────────────────────────────────────────────────

                route("/members") {

                    // GET /api/v1/campaigns/{campaignId}/members
                    get {
                        val campaignId = call.parameters["campaignId"]!!
                        val members = try {
                            CampaignService.getMembers(campaignId)
                        } catch (e: NoSuchElementException) {
                            return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        }
                        call.respond(members)
                    }

                    // POST /api/v1/campaigns/{campaignId}/members
                    post {
                        val campaignId = call.parameters["campaignId"]!!
                        val userId     = call.userId()
                        val req        = call.receive<AddMemberRequest>()

                        val member = try {
                            CampaignService.addMember(campaignId, req.userId, req.characterId, userId)
                        } catch (e: NoSuchElementException) {
                            return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        } catch (e: IllegalAccessException) {
                            return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                        } catch (e: IllegalArgumentException) {
                            return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        }
                        call.respond(HttpStatusCode.Created, member)
                    }

                    // DELETE /api/v1/campaigns/{campaignId}/members/{userId}
                    delete("/{memberId}") {
                        val campaignId = call.parameters["campaignId"]!!
                        val memberId   = call.parameters["memberId"]!!
                        val userId     = call.userId()

                        try {
                            CampaignService.removeMember(campaignId, memberId, userId)
                        } catch (e: NoSuchElementException) {
                            return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        } catch (e: IllegalAccessException) {
                            return@delete call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                        }
                        call.respond(HttpStatusCode.NoContent)
                    }
                }

                // ── Sessions ───────────────────────────────────────────────

                route("/sessions") {

                    // GET /api/v1/campaigns/{campaignId}/sessions
                    get {
                        val campaignId = call.parameters["campaignId"]!!
                        val sessions = try {
                            CampaignService.getSessions(campaignId)
                        } catch (e: NoSuchElementException) {
                            return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        }
                        call.respond(sessions)
                    }

                    // POST /api/v1/campaigns/{campaignId}/sessions
                    post {
                        val campaignId = call.parameters["campaignId"]!!
                        val userId     = call.userId()
                        val req        = call.receive<CreateSessionRequest>()

                        val session = try {
                            CampaignService.createSession(campaignId, userId, req.date)
                        } catch (e: NoSuchElementException) {
                            return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        } catch (e: IllegalAccessException) {
                            return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                        }
                        call.respond(HttpStatusCode.Created, session)
                    }

                    route("/{sessionId}") {

                        // GET /api/v1/campaigns/{campaignId}/sessions/{sessionId}
                        get {
                            val campaignId = call.parameters["campaignId"]!!
                            val sessionId  = call.parameters["sessionId"]!!
                            val session = try {
                                CampaignService.getSession(campaignId, sessionId)
                            } catch (e: NoSuchElementException) {
                                return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                            }
                            call.respond(session)
                        }

                        // PATCH /api/v1/campaigns/{campaignId}/sessions/{sessionId}/status
                        patch("/status") {
                            val campaignId = call.parameters["campaignId"]!!
                            val sessionId  = call.parameters["sessionId"]!!
                            val userId     = call.userId()
                            val req        = call.receive<UpdateSessionStatusRequest>()

                            val session = try {
                                CampaignService.updateSessionStatus(campaignId, sessionId, userId, req.status)
                            } catch (e: NoSuchElementException) {
                                return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                            } catch (e: IllegalAccessException) {
                                return@patch call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                            }
                            call.respond(session)
                        }

                        // PATCH /api/v1/campaigns/{campaignId}/sessions/{sessionId}/notes
                        patch("/notes") {
                            val campaignId = call.parameters["campaignId"]!!
                            val sessionId  = call.parameters["sessionId"]!!
                            val userId     = call.userId()
                            val req        = call.receive<UpdateSessionNotesRequest>()

                            val session = try {
                                CampaignService.updateSessionNotes(campaignId, sessionId, userId, req.notes)
                            } catch (e: NoSuchElementException) {
                                return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                            } catch (e: IllegalAccessException) {
                                return@patch call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                            }
                            call.respond(session)
                        }
                    }
                }
            }
        }
    }
}
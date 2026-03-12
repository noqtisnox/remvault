package dev.remvault.server.routing

import dev.remvault.server.dto.*
import dev.remvault.server.services.AuthService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {

        // POST /api/v1/auth/register
        post("/register") {
            val req = call.receive<RegisterRequest>()

            if (req.username.isBlank())
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username is required"))
            if (req.email.isBlank() || !req.email.contains("@"))
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Valid email is required"))
            if (req.password.length < 6)
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 6 characters"))

            val user = try {
                AuthService.register(req.username, req.email, req.password, req.role)
            } catch (e: IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }

            val token = AuthService.generateToken(user)
            call.respond(HttpStatusCode.Created, AuthResponse(
                token    = token,
                userId   = user.id,
                username = user.username,
                role     = user.role
            ))
        }

        // POST /api/v1/auth/login
        post("/login") {
            val req = call.receive<LoginRequest>()

            val user = try {
                AuthService.login(req.email, req.password)
            } catch (e: IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            }

            val token = AuthService.generateToken(user)
            call.respond(AuthResponse(
                token    = token,
                userId   = user.id,
                username = user.username,
                role     = user.role
            ))
        }

        // GET /api/v1/auth/me — requires valid token
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asString()

                val user = AuthService.findById(userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))

                call.respond(UserResponse(
                    id        = user.id,
                    username  = user.username,
                    email     = user.email,
                    role      = user.role,
                    createdAt = user.createdAt
                ))
            }
        }
    }
}
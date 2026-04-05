package dev.remvault.server.routing

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// 1. Define the Data shapes mapping to React and FastAPI
@Serializable
data class AiChatRequest(
    val message: String,
    val context: AiContext
)

@Serializable
data class AiContext(
    val name: String,
    val `class`: String,
    val level: Int
)

@Serializable
data class AiChatResponse(
    val reply: String
)

// 2. Create a global HTTP Client for Ktor to talk to Python
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

// 3. The API Gateway Route
fun Route.aiRoutes() {
    route("/ai") {
        post("/chat") {
            val request = call.receive<AiChatRequest>()

            // In Kubernetes and Docker Compose, your Python service is reachable internally here:
            // (Make sure "/chat" matches the exact endpoint in your remvault-ai/routes.py!)
            val aiServiceUrl = System.getenv("AI_SERVICE_URL") ?: "http://remvault-ai:8000/ai/chat"

            try {
                // Forward the request to Python
                val pythonResponse: AiChatResponse = httpClient.post(aiServiceUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()

                // Send Python's answer back to React
                call.respond(HttpStatusCode.OK, pythonResponse)
            } catch (e: Exception) {
                // By passing 'e' as the second argument, Ktor will print the full red stack trace!
                application.environment.log.error("AI Service connection failed", e) 
                
                call.respond(
                    HttpStatusCode.InternalServerError, 
                    mapOf("reply" to "The DM is currently asleep. (Could not reach AI service)")
                )
            }
        }
    }
}
package dev.remvault.server.dto

import dev.remvault.shared.enums.UserRole
import kotlinx.serialization.Serializable

// ── Requests ───────────────────────────────────────────────────────────────

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.PLAYER
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// ── Responses ──────────────────────────────────────────────────────────────

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val role: UserRole
)

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val createdAt: Long
)
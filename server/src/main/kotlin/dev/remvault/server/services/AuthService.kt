package dev.remvault.server.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.remvault.shared.enums.UserRole
import dev.remvault.shared.models.User
import java.util.*

object AuthService {

    // In-memory store — to be replaced with DB in Phase 2
    private val users = mutableMapOf<String, User>()

    // ── Token Generation ───────────────────────────────────────────────────

    fun generateToken(user: User): String = JWT.create()
        .withIssuer("remvault")
        .withAudience("remvault-users")
        .withClaim("userId", user.id)
        .withClaim("role", user.role.name)
        .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24h
        .sign(Algorithm.HMAC256("remvault-dev-secret-change-in-production"))

    // ── Register ───────────────────────────────────────────────────────────

    fun register(username: String, email: String, password: String, role: UserRole): User {
        if (users.values.any { it.email == email })
            throw IllegalArgumentException("Email already in use")
        if (users.values.any { it.username == username })
            throw IllegalArgumentException("Username already taken")

        val user = User(
            id = UUID.randomUUID().toString(),
            username = username,
            email = email,
            passwordHash = hashPassword(password),
            role = role
        )
        users[user.id] = user
        return user
    }

    // ── Login ──────────────────────────────────────────────────────────────

    fun login(email: String, password: String): User {
        val user = users.values.find { it.email == email }
            ?: throw IllegalArgumentException("Invalid email or password")
        if (!checkPassword(password, user.passwordHash))
            throw IllegalArgumentException("Invalid email or password")
        return user
    }

    // ── Lookup ─────────────────────────────────────────────────────────────

    fun findById(id: String): User? = users[id]

    // ── Password Hashing ───────────────────────────────────────────────────
    // Simple SHA-256 for Phase 1 — to be replaced with bcrypt in Phase 2

    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun checkPassword(password: String, hash: String): Boolean =
        hashPassword(password) == hash

    fun reset() = users.clear()

    fun findByEmail(email: String): User? = users.values.find { it.email == email }
}

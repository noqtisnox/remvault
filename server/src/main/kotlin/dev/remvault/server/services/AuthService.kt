package dev.remvault.server.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.remvault.server.database.Users
import dev.remvault.shared.enums.UserRole
import dev.remvault.shared.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object AuthService {

    // ── Helper to map a ResultRow to your User model ───────────────
    private fun ResultRow.toUser(): User = User(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        role = UserRole.valueOf(this[Users.role]),
        createdAt = this[Users.createdAt]
    )

    // ── Token Generation ───────────────────────────────────────────
    fun generateToken(user: User): String = JWT.create()
        .withIssuer("remvault")
        .withAudience("remvault-users")
        .withClaim("userId", user.id)
        .withClaim("role", user.role.name)
        .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24h
        .sign(Algorithm.HMAC256("remvault-dev-secret-change-in-production"))

    // ── Register ───────────────────────────────────────────────────
    fun register(username: String, email: String, password: String, role: UserRole): User = transaction {
        // Check for existing users
        if (Users.select { Users.email eq email }.count() > 0)
            throw IllegalArgumentException("Email already in use")
        if (Users.select { Users.username eq username }.count() > 0)
            throw IllegalArgumentException("Username already taken")

        val newId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        Users.insert {
            it[id] = newId
            it[Users.username] = username
            it[Users.email] = email
            it[passwordHash] = hashPassword(password)
            it[Users.role] = role.name
            it[createdAt] = currentTime
        }

        User(newId, username, email, hashPassword(password), role, currentTime)
    }

    // ── Login ──────────────────────────────────────────────────────
    fun login(email: String, password: String): User = transaction {
        val userRow = Users.select { Users.email eq email }.singleOrNull()
            ?: throw IllegalArgumentException("Invalid email or password")

        val user = userRow.toUser()

        if (!checkPassword(password, user.passwordHash))
            throw IllegalArgumentException("Invalid email or password")

        user
    }

    // ── Lookup ─────────────────────────────────────────────────────
    fun findById(id: String): User? = transaction {
        Users.select { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    // ── Password Hashing ───────────────────────────────────────────
    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun checkPassword(password: String, hash: String): Boolean =
        hashPassword(password) == hash
}
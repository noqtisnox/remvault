package dev.remvault.server.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.mkammerer.argon2.Argon2Factory
import dev.remvault.server.database.Users
import dev.remvault.shared.enums.UserRole
import dev.remvault.shared.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object AuthService {

    private val jwtSecret: String = System.getenv("JWT_SECRET") ?: "remvault-dev-secret-change-in-production"
    private val jwtExpirationMs: Long = System.getenv("JWT_EXPIRATION_MS")?.toLongOrNull() ?: 86_400_000L

    private val argon2 = Argon2Factory.create()

    // ── Helper to map a ResultRow to User model ───────────────
    private fun ResultRow.toUser(): User = User(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        role = UserRole.valueOf(this[Users.role]),
        createdAt = this[Users.createdAt]
    )

    // ── Token Generation ───────────────────────────────────────────
    fun generateToken(user: User): String {
        val algo = Algorithm.HMAC256(jwtSecret)
        return JWT.create()
            .withIssuer("remvault")
            .withAudience("remvault-users")
            .withClaim("userId", user.id)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationMs))
            .sign(algo)
    }

    // ── Register ───────────────────────────────────────────────────
    fun register(username: String, email: String, password: String, role: UserRole): User = transaction {
        // Check for existing users
        if (Users.selectAll().where { Users.email eq email }.count() > 0)
            throw IllegalArgumentException("Email already in use")
        if (Users.selectAll().where { Users.username eq username }.count() > 0)
            throw IllegalArgumentException("Username already taken")

        val newId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()

        val pwdHash = hashPassword(password)

        Users.insert {
            it[id] = newId
            it[Users.username] = username
            it[Users.email] = email
            it[passwordHash] = pwdHash
            it[Users.role] = role.name
            it[createdAt] = currentTime
        }

        User(newId, username, email, pwdHash, role, currentTime)
    }

    // ── Login ──────────────────────────────────────────────────────
    fun login(email: String, password: String): User = transaction {
        val userRow = Users.selectAll().where { Users.email eq email }.singleOrNull()
            ?: throw IllegalArgumentException("Invalid email or password")

        val user = userRow.toUser()

        if (!checkPassword(password, user.passwordHash))
            throw IllegalArgumentException("Invalid email or password")

        user
    }

    // ── Lookup ─────────────────────────────────────────────────────
    fun findById(id: String): User? = transaction {
        Users.selectAll().where { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findByEmail(email: String): User? = transaction {
        Users.selectAll().where { Users.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    // ── Password Hashing ───────────────────────────────────────────
    private fun hashPassword(password: String): String {
        return argon2.hash(2, 65536, 1, password.toCharArray())
    }

    private fun checkPassword(password: String, hash: String): Boolean =
        argon2.verify(hash, password.toCharArray())

    // ── Test Support ───────────────────────────────────────────────
    fun reset() = transaction {
        Users.deleteAll()
    }
}

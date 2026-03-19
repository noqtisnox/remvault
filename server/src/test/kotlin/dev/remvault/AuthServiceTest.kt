package dev.remvault

import dev.remvault.server.services.AuthService
import dev.remvault.shared.enums.UserRole
import kotlin.test.*

class AuthServiceTest {

    // Reset state between tests since AuthService is a singleton
    @BeforeTest
    fun reset() = AuthService.reset()

    // ── register() ─────────────────────────────────────────────────────────

    @Test
    fun `register creates a user`() {
        val user = AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        assertEquals("thorin", user.username)
        assertEquals("thorin@test.com", user.email)
        assertEquals(UserRole.MASTER, user.role)
    }

    @Test
    fun `register hashes password`() {
        val user = AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        assertNotEquals("password123", user.passwordHash)
    }

    @Test
    fun `register duplicate email throws`() {
        AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        assertFailsWith<IllegalArgumentException> {
            AuthService.register("thorin2", "thorin@test.com", "password456", UserRole.PLAYER)
        }
    }

    @Test
    fun `register duplicate username throws`() {
        AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        assertFailsWith<IllegalArgumentException> {
            AuthService.register("thorin", "other@test.com", "password456", UserRole.PLAYER)
        }
    }

    @Test
    fun `register multiple users with different credentials succeeds`() {
        AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        AuthService.register("bilbo", "bilbo@test.com", "password456", UserRole.PLAYER)
        assertNotNull(AuthService.findByEmail("thorin@test.com"))
        assertNotNull(AuthService.findByEmail("bilbo@test.com"))
    }

    // ── login() ────────────────────────────────────────────────────────────

    @Test
    fun `login with correct credentials returns user`() {
        AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        val user = AuthService.login("thorin@test.com", "password123")
        assertEquals("thorin", user.username)
    }

    @Test
    fun `login with wrong password throws`() {
        AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        assertFailsWith<IllegalArgumentException> {
            AuthService.login("thorin@test.com", "wrongpassword")
        }
    }

    @Test
    fun `login with unknown email throws`() {
        assertFailsWith<IllegalArgumentException> {
            AuthService.login("nobody@test.com", "password123")
        }
    }

    // ── findById() ─────────────────────────────────────────────────────────

    @Test
    fun `findById returns user when exists`() {
        val registered = AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        val found = AuthService.findById(registered.id)
        assertNotNull(found)
        assertEquals("thorin", found.username)
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(AuthService.findById("nonexistent-id"))
    }

    // ── generateToken() ────────────────────────────────────────────────────

    @Test
    fun `generateToken returns non-blank string`() {
        val user = AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        val token = AuthService.generateToken(user)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateToken contains three JWT parts`() {
        val user = AuthService.register("thorin", "thorin@test.com", "password123", UserRole.MASTER)
        val token = AuthService.generateToken(user)
        assertEquals(3, token.split(".").size)
    }
}

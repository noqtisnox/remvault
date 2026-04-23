package dev.remvault

import dev.testutils.TestDatabase
import dev.remvault.server.dto.CreateCharacterRequest
import dev.remvault.server.services.AuthService
import dev.remvault.server.services.CharacterService
import dev.remvault.shared.enums.UserRole
import kotlin.test.*

class CharacterServiceTest {

    private lateinit var userId: String
    private lateinit var otherUserId: String

    @BeforeTest
    fun reset() {
        TestDatabase.init()
        AuthService.reset()
        CharacterService.reset()
        userId = AuthService.register("thorin", "thorin@test.com", "pass123", UserRole.PLAYER).id
        otherUserId = AuthService.register("bilbo", "bilbo@test.com", "pass456", UserRole.PLAYER).id
    }

    // ── createCharacter() ──────────────────────────────────────────────────

    @Test
    fun `createCharacter returns a sheet`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertEquals("Gandalf", sheet.character.name)
        assertEquals("Human", sheet.character.race)
        assertEquals("wizard", sheet.character.characterClass)
        assertEquals(1, sheet.character.level)
    }

    @Test
    fun `createCharacter rolls valid stats`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        listOf(
            sheet.stats.strength, sheet.stats.dexterity, sheet.stats.constitution,
            sheet.stats.intelligence, sheet.stats.wisdom, sheet.stats.charisma
        )
            .forEach { assertTrue(it in 3..18, "Stat $it out of range") }
    }

    @Test
    fun `createCharacter computes correct HP for wizard`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertTrue(sheet.hitPoints.maximum > 0)
        assertEquals(sheet.hitPoints.maximum, sheet.hitPoints.current)
    }

    @Test
    fun `createCharacter seeds all 18 skills`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertEquals(18, sheet.skills.size)
    }

    @Test
    fun `createCharacter proficiency bonus at level 1 is 2`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertEquals(2, sheet.proficiencyBonus)
    }

    // ── getCharacter() ─────────────────────────────────────────────────

    @Test
    fun `getCharacter returns sheet for valid id`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val created = CharacterService.createCharacter(userId, req)
        val fetched = CharacterService.getCharacter(created.character.id)
        assertNotNull(fetched)
        assertEquals("Gandalf", fetched.character.name)
    }

    @Test
    fun `getCharacter returns null for unknown id`() {
        assertNull(CharacterService.getCharacter("nonexistent-id"))
    }

    // ── getCharactersByUser() ──────────────────────────────────────────────

    @Test
    fun `getCharactersByUser returns only own characters`() {
        val req1 = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val req2 = CreateCharacterRequest(
            name = "Aragorn",
            race = "Human",
            characterClass = "fighter",
            background = "Soldier"
        )
        val req3 = CreateCharacterRequest(
            name = "Legolas",
            race = "Elf",
            characterClass = "ranger",
            background = "Outlander"
        )

        CharacterService.createCharacter(userId, req1)
        CharacterService.createCharacter(userId, req2)
        CharacterService.createCharacter(otherUserId, req3)

        val mine = CharacterService.getCharactersByUser(userId)
        assertEquals(2, mine.size)
        assertTrue(mine.all { it.character.userId == userId })
    }

    // ── updateHitPoints() ──────────────────────────────────────────────────

    @Test
    fun `updateHitPoints sets current HP`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        val hp = CharacterService.updateHitPoints(sheet.character.id, 4)
        assertEquals(4, hp.current)
    }

    @Test
    fun `updateHitPoints clamps to zero`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        val hp = CharacterService.updateHitPoints(sheet.character.id, -5)
        assertEquals(0, hp.current)
    }

    @Test
    fun `updateHitPoints clamps to maximum`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        val max = sheet.hitPoints.maximum
        val hp = CharacterService.updateHitPoints(sheet.character.id, max + 99)
        assertEquals(max, hp.current)
    }

    @Test
    fun `updateHitPoints throws for unknown character`() {
        assertFailsWith<NoSuchElementException> {
            CharacterService.updateHitPoints("nonexistent-id", 5)
        }
    }

    // ── updateCharacter() ──────────────────────────────────────────────────

    @Test
    fun `updateCharacter changes name`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        val updated = CharacterService.updateCharacter(sheet.character.id, userId, name = "Gandalf the White")
        assertEquals("Gandalf the White", updated.character.name)
    }

    @Test
    fun `updateCharacter by non-owner throws`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertFailsWith<IllegalAccessException> {
            CharacterService.updateCharacter(sheet.character.id, otherUserId, name = "Impostor")
        }
    }

    @Test
    fun `updateCharacter xp triggers level up`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        val updated = CharacterService.updateCharacter(sheet.character.id, userId, experiencePoints = 300)
        assertEquals(2, updated.character.level)
    }

    // ── deleteCharacter() ──────────────────────────────────────────────────

    @Test
    fun `deleteCharacter removes character`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        CharacterService.deleteCharacter(sheet.character.id, userId)
        assertNull(CharacterService.getCharacter(sheet.character.id))
    }

    @Test
    fun `deleteCharacter by non-owner throws`() {
        val req = CreateCharacterRequest(
            name = "Gandalf",
            race = "Human",
            characterClass = "wizard",
            background = "Sage"
        )
        val sheet = CharacterService.createCharacter(userId, req)
        assertFailsWith<IllegalAccessException> {
            CharacterService.deleteCharacter(sheet.character.id, otherUserId)
        }
    }

    @Test
    fun `deleteCharacter unknown id throws`() {
        assertFailsWith<NoSuchElementException> {
            CharacterService.deleteCharacter("nonexistent-id", userId)
        }
    }
}

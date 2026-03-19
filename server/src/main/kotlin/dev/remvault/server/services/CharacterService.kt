package dev.remvault.server.services

import dev.remvault.shared.enums.CharacterStatus
import dev.remvault.shared.enums.Proficiency
import dev.remvault.shared.models.*
import dev.remvault.shared.rules.RulesEngine
import kotlinx.serialization.Serializable
import java.util.UUID

object CharacterService {

    // ── In-memory stores ───────────────────────────────────────────────────

    private val characters = mutableMapOf<String, Character>()
    private val stats = mutableMapOf<String, Stats>()
    private val hitPoints = mutableMapOf<String, HitPoints>()
    private val deathSaves = mutableMapOf<String, DeathSaves>()
    private val skills = mutableMapOf<String, MutableList<SkillProficiency>>()
    private val spellSlots = mutableMapOf<String, MutableList<SpellSlot>>()
    private val inventory = mutableMapOf<String, MutableList<InventoryEntry>>()

    // ── Create ─────────────────────────────────────────────────────────────

    fun createCharacter(
        userId: String,
        name: String,
        race: String,
        characterClass: String,
        background: String,
        campaignId: String? = null,
        alignment: String? = null,
        subrace: String? = null,
        subclass: String? = null,
    ): CharacterSheet {
        val id = UUID.randomUUID().toString()

        // Roll stats via DiceService
        val rolled = DiceService.rollStatBlock()
        val charStats = Stats(
            characterId = id,
            strength = rolled.strength,
            dexterity = rolled.dexterity,
            constitution = rolled.constitution,
            intelligence = rolled.intelligence,
            wisdom = rolled.wisdom,
            charisma = rolled.charisma
        )

        // Calculate HP via RulesEngine
        val hitDie = RulesEngine.hitDie(characterClass)
        val maxHp = RulesEngine.maxHitPoints(1, hitDie, charStats.constitution)

        val character = Character(
            id = id,
            userId = userId,
            campaignId = campaignId,
            name = name,
            race = race,
            subrace = subrace,
            characterClass = characterClass,
            subclass = subclass,
            level = 1,
            background = background,
            alignment = alignment,
            status = CharacterStatus.ALIVE
        )

        val hp = HitPoints(
            characterId = id,
            maximum = maxHp,
            current = maxHp,
            hitDiceTotal = 1
        )

        // Seed default skills with no proficiency
        val defaultSkills = defaultSkillList(id)

        characters[id] = character
        stats[id] = charStats
        hitPoints[id] = hp
        deathSaves[id] = DeathSaves(characterId = id)
        skills[id] = defaultSkills.toMutableList()
        spellSlots[id] = mutableListOf()
        inventory[id] = mutableListOf()

        return buildSheet(character, charStats, hp)
    }

    // ── Read ───────────────────────────────────────────────────────────────

    fun getCharacter(characterId: String): CharacterSheet? {
        val character = characters[characterId] ?: return null
        val charStats = stats[characterId] ?: return null
        val hp = hitPoints[characterId] ?: return null
        return buildSheet(character, charStats, hp)
    }

    fun getCharactersByUser(userId: String): List<CharacterSheet> =
        characters.values
            .filter { it.userId == userId }
            .mapNotNull { getCharacter(it.id) }

    fun getCharactersByCampaign(campaignId: String): List<CharacterSheet> =
        characters.values
            .filter { it.campaignId == campaignId }
            .mapNotNull { getCharacter(it.id) }

    // ── Update ─────────────────────────────────────────────────────────────

    fun updateHitPoints(characterId: String, newCurrent: Int): HitPoints {
        val hp = hitPoints[characterId]
            ?: throw NoSuchElementException("Character not found")
        val updated = hp.copy(current = newCurrent.coerceIn(0, hp.maximum))
        hitPoints[characterId] = updated
        return updated
    }

    fun updateCharacter(
        characterId: String,
        userId: String,
        name: String? = null,
        alignment: String? = null,
        subclass: String? = null,
        experiencePoints: Int? = null,
    ): CharacterSheet {
        val character = characters[characterId]
            ?: throw NoSuchElementException("Character not found")
        if (character.userId != userId)
            throw IllegalAccessException("You don't own this character")

        val updated = character.copy(
            name = name ?: character.name,
            alignment = alignment ?: character.alignment,
            subclass = subclass ?: character.subclass,
            experiencePoints = experiencePoints ?: character.experiencePoints,
            // Auto level-up based on XP
            level = experiencePoints
                ?.let { RulesEngine.levelFromXp(it) }
                ?: character.level
        )

        characters[characterId] = updated
        return buildSheet(updated, stats[characterId]!!, hitPoints[characterId]!!)
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    fun deleteCharacter(characterId: String, userId: String) {
        val character = characters[characterId]
            ?: throw NoSuchElementException("Character not found")
        if (character.userId != userId)
            throw IllegalAccessException("You don't own this character")

        characters.remove(characterId)
        stats.remove(characterId)
        hitPoints.remove(characterId)
        deathSaves.remove(characterId)
        skills.remove(characterId)
        spellSlots.remove(characterId)
        inventory.remove(characterId)
    }

    // ── Sheet Builder ──────────────────────────────────────────────────────
    // Assembles all computed values in one place

    private fun buildSheet(
        character: Character,
        charStats: Stats,
        hp: HitPoints
    ): CharacterSheet {
        val level = character.level
        val prof = RulesEngine.proficiencyBonus(level)

        return CharacterSheet(
            character = character,
            stats = charStats,
            hitPoints = hp,
            proficiencyBonus = prof,
            strModifier = RulesEngine.modifier(charStats.strength),
            dexModifier = RulesEngine.modifier(charStats.dexterity),
            conModifier = RulesEngine.modifier(charStats.constitution),
            intModifier = RulesEngine.modifier(charStats.intelligence),
            wisModifier = RulesEngine.modifier(charStats.wisdom),
            chaModifier = RulesEngine.modifier(charStats.charisma),
            passivePerception = RulesEngine.passivePerception(charStats.wisdom, level, false),
            carryingCapacity = RulesEngine.carryingCapacity(charStats.strength),
            skills = skills[character.id] ?: emptyList(),
            spellSlots = spellSlots[character.id] ?: emptyList(),
            inventory = inventory[character.id] ?: emptyList()
        )
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun defaultSkillList(characterId: String) = listOf(
        SkillProficiency(characterId, "Acrobatics", "dexterity", Proficiency.NONE),
        SkillProficiency(characterId, "Animal Handling", "wisdom", Proficiency.NONE),
        SkillProficiency(characterId, "Arcana", "intelligence", Proficiency.NONE),
        SkillProficiency(characterId, "Athletics", "strength", Proficiency.NONE),
        SkillProficiency(characterId, "Deception", "charisma", Proficiency.NONE),
        SkillProficiency(characterId, "History", "intelligence", Proficiency.NONE),
        SkillProficiency(characterId, "Insight", "wisdom", Proficiency.NONE),
        SkillProficiency(characterId, "Intimidation", "charisma", Proficiency.NONE),
        SkillProficiency(characterId, "Investigation", "intelligence", Proficiency.NONE),
        SkillProficiency(characterId, "Medicine", "wisdom", Proficiency.NONE),
        SkillProficiency(characterId, "Nature", "intelligence", Proficiency.NONE),
        SkillProficiency(characterId, "Perception", "wisdom", Proficiency.NONE),
        SkillProficiency(characterId, "Performance", "charisma", Proficiency.NONE),
        SkillProficiency(characterId, "Persuasion", "charisma", Proficiency.NONE),
        SkillProficiency(characterId, "Religion", "intelligence", Proficiency.NONE),
        SkillProficiency(characterId, "Sleight of Hand", "dexterity", Proficiency.NONE),
        SkillProficiency(characterId, "Stealth", "dexterity", Proficiency.NONE),
        SkillProficiency(characterId, "Survival", "wisdom", Proficiency.NONE),
    )

    fun reset() {
        characters.clear()
        stats.clear()
        hitPoints.clear()
        deathSaves.clear()
        skills.clear()
        spellSlots.clear()
        inventory.clear()
    }
}

// ── CharacterSheet response model ──────────────────────────────────────────
// Lives here since it's server-only — not shared with other modules

@Serializable
data class CharacterSheet(
    val character: Character,
    val stats: Stats,
    val hitPoints: HitPoints,
    val proficiencyBonus: Int,
    val strModifier: Int,
    val dexModifier: Int,
    val conModifier: Int,
    val intModifier: Int,
    val wisModifier: Int,
    val chaModifier: Int,
    val passivePerception: Int,
    val carryingCapacity: Int,
    val skills: List<SkillProficiency>,
    val spellSlots: List<SpellSlot>,
    val inventory: List<InventoryEntry>
)

package dev.remvault.server.services

import kotlinx.serialization.Serializable

import dev.remvault.server.database.*
import dev.remvault.shared.enums.CharacterStatus
import dev.remvault.shared.enums.Proficiency
import dev.remvault.shared.models.*
import dev.remvault.shared.rules.RulesEngine
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

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

object CharacterService {

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun ResultRow.toCharacter() = Character(
        id = this[Characters.id],
        userId = this[Characters.userId],
        campaignId = this[Characters.campaignId],
        name = this[Characters.name],
        race = this[Characters.race],
        subrace = this[Characters.subrace],
        characterClass = this[Characters.characterClass],
        subclass = this[Characters.subclass],
        level = this[Characters.level],
        background = this[Characters.background],
        alignment = this[Characters.alignment],
        experiencePoints = this[Characters.experiencePoints],
        inspiration = this[Characters.inspiration],
        status = CharacterStatus.valueOf(this[Characters.status])
    )

    private fun ResultRow.toStats() = Stats(
        characterId = this[CharacterStats.characterId],
        strength = this[CharacterStats.strength],
        dexterity = this[CharacterStats.dexterity],
        constitution = this[CharacterStats.constitution],
        intelligence = this[CharacterStats.intelligence],
        wisdom = this[CharacterStats.wisdom],
        charisma = this[CharacterStats.charisma]
    )

    private fun ResultRow.toHitPoints() = HitPoints(
        characterId = this[CharacterHitPoints.characterId],
        maximum = this[CharacterHitPoints.maximum],
        current = this[CharacterHitPoints.current],
        temporary = this[CharacterHitPoints.temporary],
        hitDiceTotal = this[CharacterHitPoints.hitDiceTotal],
        hitDiceUsed = this[CharacterHitPoints.hitDiceUsed]
    )

    // ── Create ─────────────────────────────────────────────────────────────

    fun createCharacter(
        userId: String, name: String, race: String, characterClass: String, background: String,
        campaignId: String? = null, alignment: String? = null, subrace: String? = null, subclass: String? = null
    ): CharacterSheet = transaction {
        val charId = UUID.randomUUID().toString()

        val rolled = DiceService.rollStatBlock()
        val hitDie = RulesEngine.hitDie(characterClass)
        val maxHp = RulesEngine.maxHitPoints(1, hitDie, rolled.constitution)

        Characters.insert {
            it[id] = charId
            it[Characters.userId] = userId
            it[Characters.campaignId] = campaignId
            it[Characters.name] = name
            it[Characters.race] = race
            it[Characters.subrace] = subrace
            it[Characters.characterClass] = characterClass
            it[Characters.subclass] = subclass
            it[level] = 1
            it[Characters.background] = background
            it[Characters.alignment] = alignment
            it[status] = CharacterStatus.ALIVE.name
        }

        CharacterStats.insert {
            it[characterId] = charId
            it[strength] = rolled.strength
            it[dexterity] = rolled.dexterity
            it[constitution] = rolled.constitution
            it[intelligence] = rolled.intelligence
            it[wisdom] = rolled.wisdom
            it[charisma] = rolled.charisma
        }

        CharacterHitPoints.insert {
            it[characterId] = charId
            it[maximum] = maxHp
            it[current] = maxHp
            it[hitDiceTotal] = 1
        }

        CharacterDeathSaves.insert { it[characterId] = charId }

        defaultSkillList(charId).forEach { skill ->
            CharacterSkills.insert {
                it[characterId] = charId
                it[skillName] = skill.skillName
                it[stat] = skill.stat
                it[proficiency] = skill.proficiency.name
            }
        }

        getCharacter(charId)!!
    }

    // ── Read ───────────────────────────────────────────────────────────────

    fun getCharacter(characterId: String): CharacterSheet? = transaction {
        val characterRow = Characters.select { Characters.id eq characterId }.singleOrNull() ?: return@transaction null
        val statsRow = CharacterStats.select { CharacterStats.characterId eq characterId }.single()
        val hpRow = CharacterHitPoints.select { CharacterHitPoints.characterId eq characterId }.single()

        val skills = CharacterSkills.select { CharacterSkills.characterId eq characterId }.map {
            SkillProficiency(it[CharacterSkills.characterId], it[CharacterSkills.skillName], it[CharacterSkills.stat], Proficiency.valueOf(it[CharacterSkills.proficiency]))
        }

        val spells = CharacterSpellSlots.select { CharacterSpellSlots.characterId eq characterId }.map {
            SpellSlot(it[CharacterSpellSlots.characterId], it[CharacterSpellSlots.level], it[CharacterSpellSlots.maximum], it[CharacterSpellSlots.current])
        }

        val inventory = CharacterInventory.select { CharacterInventory.characterId eq characterId }.map {
            InventoryEntry(it[CharacterInventory.id], it[CharacterInventory.characterId], it[CharacterInventory.itemName], it[CharacterInventory.quantity], it[CharacterInventory.equipped], it[CharacterInventory.attuned], it[CharacterInventory.notes])
        }

        buildSheet(characterRow.toCharacter(), statsRow.toStats(), hpRow.toHitPoints(), skills, spells, inventory)
    }

    fun getCharactersByUser(userId: String): List<CharacterSheet> = transaction {
        Characters.select { Characters.userId eq userId }.mapNotNull { getCharacter(it[Characters.id]) }
    }

    fun getCharactersByCampaign(campaignId: String): List<CharacterSheet> = transaction {
        Characters.select { Characters.campaignId eq campaignId }.mapNotNull { getCharacter(it[Characters.id]) }
    }

    // ── Update ─────────────────────────────────────────────────────────────

    fun updateHitPoints(characterId: String, newCurrent: Int): HitPoints = transaction {
        val hpRow = CharacterHitPoints.select { CharacterHitPoints.characterId eq characterId }.singleOrNull()
            ?: throw NoSuchElementException("Character not found")
        val max = hpRow[CharacterHitPoints.maximum]
        val safeCurrent = newCurrent.coerceIn(0, max)

        CharacterHitPoints.update({ CharacterHitPoints.characterId eq characterId }) {
            it[current] = safeCurrent
        }
        CharacterHitPoints.select { CharacterHitPoints.characterId eq characterId }.single().toHitPoints()
    }

    fun updateCharacter(
        characterId: String, userId: String, name: String? = null, alignment: String? = null,
        subclass: String? = null, experiencePoints: Int? = null,
    ): CharacterSheet = transaction {
        val charRow = Characters.select { Characters.id eq characterId }.singleOrNull()
            ?: throw NoSuchElementException("Character not found")
        if (charRow[Characters.userId] != userId) throw IllegalAccessException("You don't own this character")

        val newLevel = experiencePoints?.let { RulesEngine.levelFromXp(it) } ?: charRow[Characters.level]

        Characters.update({ Characters.id eq characterId }) {
            if (name != null) it[Characters.name] = name
            if (alignment != null) it[Characters.alignment] = alignment
            if (subclass != null) it[Characters.subclass] = subclass
            if (experiencePoints != null) it[Characters.experiencePoints] = experiencePoints
            it[level] = newLevel
        }
        getCharacter(characterId)!!
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    fun deleteCharacter(characterId: String, userId: String) = transaction {
        val charRow = Characters.select { Characters.id eq characterId }.singleOrNull()
            ?: throw NoSuchElementException("Character not found")
        if (charRow[Characters.userId] != userId) throw IllegalAccessException("You don't own this character")

        // Manually cascade deletes to child tables
        CharacterStats.deleteWhere { CharacterStats.characterId eq characterId }
        CharacterHitPoints.deleteWhere { CharacterHitPoints.characterId eq characterId }
        CharacterDeathSaves.deleteWhere { CharacterDeathSaves.characterId eq characterId }
        CharacterSkills.deleteWhere { CharacterSkills.characterId eq characterId }
        CharacterSpellSlots.deleteWhere { CharacterSpellSlots.characterId eq characterId }
        CharacterInventory.deleteWhere { CharacterInventory.characterId eq characterId }

        // Finally, delete the parent
        Characters.deleteWhere { Characters.id eq characterId }
    }

    // ── Sheet Builder & Helpers ────────────────────────────────────────────

    private fun buildSheet(
        character: Character, charStats: Stats, hp: HitPoints,
        skills: List<SkillProficiency>, spells: List<SpellSlot>, inventory: List<InventoryEntry>
    ): CharacterSheet {
        val level = character.level
        val prof = RulesEngine.proficiencyBonus(level)

        return CharacterSheet(
            character = character, stats = charStats, hitPoints = hp, proficiencyBonus = prof,
            strModifier = RulesEngine.modifier(charStats.strength), dexModifier = RulesEngine.modifier(charStats.dexterity),
            conModifier = RulesEngine.modifier(charStats.constitution), intModifier = RulesEngine.modifier(charStats.intelligence),
            wisModifier = RulesEngine.modifier(charStats.wisdom), chaModifier = RulesEngine.modifier(charStats.charisma),
            passivePerception = RulesEngine.passivePerception(charStats.wisdom, level, false),
            carryingCapacity = RulesEngine.carryingCapacity(charStats.strength),
            skills = skills, spellSlots = spells, inventory = inventory
        )
    }

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
}
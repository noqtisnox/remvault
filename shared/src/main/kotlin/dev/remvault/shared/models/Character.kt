package dev.remvault.shared.models

import dev.remvault.shared.enums.CharacterStatus
import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val userId: String,
    val campaignId: String?,
    val name: String,
    val race: String,
    val subrace: String? = null,
    val characterClass: String,
    val subclass: String? = null,
    val level: Int = 1,
    val background: String,
    val alignment: String? = null,
    val experiencePoints: Int = 0,
    val inspiration: Boolean = false,
    val status: CharacterStatus = CharacterStatus.ALIVE
)

@Serializable
data class Stats(
    val characterId: String,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int
)

@Serializable
data class HitPoints(
    val characterId: String,
    val maximum: Int,
    val current: Int,
    val temporary: Int = 0,
    val hitDiceTotal: Int,
    val hitDiceUsed: Int = 0
)

@Serializable
data class DeathSaves(
    val characterId: String,
    val successes: Int = 0,
    val failures: Int = 0
)

@Serializable
data class SkillProficiency(
    val characterId: String,
    val skillName: String,
    val stat: String,
    val proficiency: dev.remvault.shared.enums.Proficiency
)

@Serializable
data class SpellSlot(
    val characterId: String,
    val level: Int,
    val maximum: Int,
    val current: Int
)

@Serializable
data class InventoryEntry(
    val id: String,
    val characterId: String,
    val itemName: String,
    val quantity: Int = 1,
    val equipped: Boolean = false,
    val attuned: Boolean = false,
    val notes: String? = null
)
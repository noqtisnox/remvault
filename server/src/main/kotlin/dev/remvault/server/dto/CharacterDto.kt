package dev.remvault.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val race: String,
    val characterClass: String,
    val background: String,
    val campaignId: String? = null,
    val alignment: String? = null,
    val subrace: String? = null,
    val subclass: String? = null,

    val level: Int = 1,
    val experiencePoints: Int = 0,
    val inspiration: Boolean = false,

    // Optional manual stats (if null, the server will roll 4d6 drop lowest)
    val strength: Int? = null,
    val dexterity: Int? = null,
    val constitution: Int? = null,
    val intelligence: Int? = null,
    val wisdom: Int? = null,
    val charisma: Int? = null
)

@Serializable
data class UpdateCharacterRequest(
    val name: String? = null,
    val alignment: String? = null,
    val subclass: String? = null,
    val experiencePoints: Int? = null,
)

@Serializable
data class UpdateHitPointsRequest(
    val current: Int
)
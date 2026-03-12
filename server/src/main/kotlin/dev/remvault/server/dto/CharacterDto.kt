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
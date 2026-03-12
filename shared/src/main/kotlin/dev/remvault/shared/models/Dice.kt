package dev.remvault.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Roll(
    val id: String,
    val sessionId: String?,
    val actorId: String,
    val expression: String,
    val results: List<Int>,
    val total: Int,
    val purpose: String? = null,
    val advantage: Advantage = Advantage.NONE,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class Advantage { NONE, ADVANTAGE, DISADVANTAGE }
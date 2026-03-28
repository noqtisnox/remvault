package dev.remvault.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class RollRequest(val expression: String) // e.g., "2d6+3"

@Serializable
data class RollResponse(
    val expression: String,
    val rolls: List<Int>,
    val modifier: Int,
    val total: Int
)
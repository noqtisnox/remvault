package dev.remvault.server.dto

import dev.remvault.shared.enums.SessionStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateCampaignRequest(
    val name: String,
    val description: String? = null,
    val setting: String? = null
)

@Serializable
data class AddMemberRequest(
    val userId: String,
    val characterId: String? = null
)

@Serializable
data class CreateSessionRequest(
    val date: Long
)

@Serializable
data class UpdateSessionStatusRequest(
    val status: SessionStatus
)

@Serializable
data class UpdateSessionNotesRequest(
    val notes: String
)
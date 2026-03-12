package dev.remvault.shared.models

import dev.remvault.shared.enums.CampaignStatus
import dev.remvault.shared.enums.SessionStatus
import kotlinx.serialization.Serializable

@Serializable
data class Campaign(
    val id: String,
    val name: String,
    val masterId: String,
    val description: String? = null,
    val setting: String? = null,
    val status: CampaignStatus = CampaignStatus.ACTIVE
)

@Serializable
data class CampaignMember(
    val userId: String,
    val campaignId: String,
    val characterId: String? = null,
    val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Session(
    val id: String,
    val campaignId: String,
    val date: Long,
    val status: SessionStatus = SessionStatus.PLANNED,
    val rawNotes: String? = null,
    val aiSummary: String? = null
)
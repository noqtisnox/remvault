package dev.remvault.server.services

import dev.remvault.shared.enums.CampaignStatus
import dev.remvault.shared.enums.SessionStatus
import dev.remvault.shared.models.*
import java.util.UUID

object CampaignService {

    // ── In-memory stores ───────────────────────────────────────────────────

    private val campaigns = mutableMapOf<String, Campaign>()
    private val members   = mutableMapOf<String, MutableList<CampaignMember>>() // key: campaignId
    private val sessions  = mutableMapOf<String, MutableList<Session>>()        // key: campaignId

    // ── Campaigns ──────────────────────────────────────────────────────────

    fun createCampaign(
        masterId: String,
        name: String,
        description: String? = null,
        setting: String? = null
    ): Campaign {
        val campaign = Campaign(
            id          = UUID.randomUUID().toString(),
            name        = name,
            masterId    = masterId,
            description = description,
            setting     = setting,
            status      = CampaignStatus.ACTIVE
        )
        campaigns[campaign.id]  = campaign
        members[campaign.id]    = mutableListOf()
        sessions[campaign.id]   = mutableListOf()
        return campaign
    }

    fun getCampaign(campaignId: String): Campaign =
        campaigns[campaignId]
            ?: throw NoSuchElementException("Campaign not found")

    fun getCampaignsByMaster(masterId: String): List<Campaign> =
        campaigns.values.filter { it.masterId == masterId }

    fun getCampaignsByPlayer(userId: String): List<Campaign> =
        members.entries
            .filter { (_, memberList) -> memberList.any { it.userId == userId } }
            .mapNotNull { (campaignId, _) -> campaigns[campaignId] }

    fun archiveCampaign(campaignId: String, masterId: String): Campaign {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can archive this campaign")
        val updated = campaign.copy(status = CampaignStatus.ARCHIVED)
        campaigns[campaignId] = updated
        return updated
    }

    // ── Members ────────────────────────────────────────────────────────────

    fun addMember(
        campaignId: String,
        userId: String,
        characterId: String? = null,
        requesterId: String
    ): CampaignMember {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != requesterId)
            throw IllegalAccessException("Only the master can invite players")

        val campaignMembers = members[campaignId]!!
        if (campaignMembers.any { it.userId == userId })
            throw IllegalArgumentException("Player is already in this campaign")

        // Validate character belongs to user if provided
        if (characterId != null) {
            val sheet = CharacterService.getCharacter(characterId)
                ?: throw NoSuchElementException("Character not found")
            if (sheet.character.userId != userId)
                throw IllegalArgumentException("Character does not belong to this player")
        }

        val member = CampaignMember(
            userId     = userId,
            campaignId = campaignId,
            characterId = characterId
        )
        campaignMembers.add(member)
        return member
    }

    fun getMembers(campaignId: String): List<CampaignMember> {
        getCampaign(campaignId) // validates existence
        return members[campaignId] ?: emptyList()
    }

    fun removeMember(campaignId: String, userId: String, requesterId: String) {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != requesterId && userId != requesterId)
            throw IllegalAccessException("Only the master or the player themselves can remove a member")
        members[campaignId]?.removeIf { it.userId == userId }
            ?: throw NoSuchElementException("Campaign not found")
    }

    // ── Sessions ───────────────────────────────────────────────────────────

    fun createSession(
        campaignId: String,
        masterId: String,
        date: Long,
    ): Session {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can create sessions")

        val session = Session(
            id         = UUID.randomUUID().toString(),
            campaignId = campaignId,
            date       = date,
            status     = SessionStatus.PLANNED
        )
        sessions[campaignId]!!.add(session)
        return session
    }

    fun getSessions(campaignId: String): List<Session> {
        getCampaign(campaignId) // validates existence
        return sessions[campaignId] ?: emptyList()
    }

    fun getSession(campaignId: String, sessionId: String): Session =
        sessions[campaignId]
            ?.find { it.id == sessionId }
            ?: throw NoSuchElementException("Session not found")

    fun updateSessionStatus(
        campaignId: String,
        sessionId: String,
        masterId: String,
        status: SessionStatus
    ): Session {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can update session status")

        val sessionList = sessions[campaignId]!!
        val index = sessionList.indexOfFirst { it.id == sessionId }
        if (index == -1) throw NoSuchElementException("Session not found")

        val updated = sessionList[index].copy(status = status)
        sessionList[index] = updated
        return updated
    }

    fun updateSessionNotes(
        campaignId: String,
        sessionId: String,
        masterId: String,
        notes: String
    ): Session {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can update session notes")

        val sessionList = sessions[campaignId]!!
        val index = sessionList.indexOfFirst { it.id == sessionId }
        if (index == -1) throw NoSuchElementException("Session not found")

        val updated = sessionList[index].copy(rawNotes = notes)
        sessionList[index] = updated
        return updated
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    fun isMember(campaignId: String, userId: String): Boolean =
        members[campaignId]?.any { it.userId == userId } ?: false

    fun isMaster(campaignId: String, userId: String): Boolean =
        campaigns[campaignId]?.masterId == userId
}
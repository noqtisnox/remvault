package dev.remvault.server.services

import dev.remvault.server.database.CampaignMembers
import dev.remvault.server.database.Campaigns
import dev.remvault.server.database.Sessions
import dev.remvault.shared.enums.CampaignStatus
import dev.remvault.shared.enums.SessionStatus
import dev.remvault.shared.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object CampaignService {

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun ResultRow.toCampaign() = Campaign(
        id = this[Campaigns.id],
        name = this[Campaigns.name],
        masterId = this[Campaigns.masterId],
        description = this[Campaigns.description],
        setting = this[Campaigns.setting],
        status = CampaignStatus.valueOf(this[Campaigns.status])
    )

    private fun ResultRow.toCampaignMember() = CampaignMember(
        userId = this[CampaignMembers.userId],
        campaignId = this[CampaignMembers.campaignId],
        characterId = this[CampaignMembers.characterId],
        joinedAt = this[CampaignMembers.joinedAt]
    )

    private fun ResultRow.toSession() = Session(
        id = this[Sessions.id],
        campaignId = this[Sessions.campaignId],
        date = this[Sessions.date],
        status = SessionStatus.valueOf(this[Sessions.status]),
        rawNotes = this[Sessions.rawNotes],
        aiSummary = this[Sessions.aiSummary]
    )

    // ── Campaigns ──────────────────────────────────────────────────────────

    fun createCampaign(
        masterId: String, name: String, description: String? = null, setting: String? = null
    ): Campaign = transaction {
        val newId = UUID.randomUUID().toString()
        Campaigns.insert {
            it[id] = newId
            it[Campaigns.name] = name
            it[Campaigns.masterId] = masterId
            it[Campaigns.description] = description
            it[Campaigns.setting] = setting
            it[status] = CampaignStatus.ACTIVE.name
        }
        getCampaign(newId)
    }

    fun getCampaign(campaignId: String): Campaign = transaction {
        Campaigns.selectAll().where { Campaigns.id eq campaignId }
            .map { it.toCampaign() }
            .singleOrNull() ?: throw NoSuchElementException("Campaign not found")
    }

    fun getCampaignsByMaster(masterId: String): List<Campaign> = transaction {
        Campaigns.selectAll().where { Campaigns.masterId eq masterId }
            .map { it.toCampaign() }
    }

    fun getCampaignsByPlayer(userId: String): List<Campaign> = transaction {
        (Campaigns innerJoin CampaignMembers)
            .selectAll().where { CampaignMembers.userId eq userId }
            .map { it.toCampaign() }
    }

    fun archiveCampaign(campaignId: String, masterId: String): Campaign = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can archive this campaign")

        Campaigns.update({ Campaigns.id eq campaignId }) {
            it[status] = CampaignStatus.ARCHIVED.name
        }
        getCampaign(campaignId)
    }

    // ── Members ────────────────────────────────────────────────────────────

    fun addMember(
        campaignId: String, userId: String, characterId: String? = null, requesterId: String
    ): CampaignMember = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != requesterId)
            throw IllegalAccessException("Only the master can invite players")

        if (isMember(campaignId, userId))
            throw IllegalArgumentException("Player is already in this campaign")

        if (characterId != null) {
            val sheet = CharacterService.getCharacter(characterId)
                ?: throw NoSuchElementException("Character not found")
            if (sheet.character.userId != userId)
                throw IllegalArgumentException("Character does not belong to this player")
        }

        val currentTime = System.currentTimeMillis()
        CampaignMembers.insert {
            it[CampaignMembers.userId] = userId
            it[CampaignMembers.campaignId] = campaignId
            it[CampaignMembers.characterId] = characterId
            it[joinedAt] = currentTime
        }

        CampaignMember(userId, campaignId, characterId, currentTime)
    }

    fun getMembers(campaignId: String): List<CampaignMember> = transaction {
        getCampaign(campaignId) // validates existence
        CampaignMembers.selectAll().where { CampaignMembers.campaignId eq campaignId }
            .map { it.toCampaignMember() }
    }

    fun removeMember(campaignId: String, userId: String, requesterId: String) = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != requesterId && userId != requesterId)
            throw IllegalAccessException("Only the master or the player themselves can remove a member")

        val deletedCount = CampaignMembers.deleteWhere {
            (CampaignMembers.campaignId eq campaignId) and (CampaignMembers.userId eq userId)
        }
        if (deletedCount == 0) throw NoSuchElementException("Campaign member not found")
    }

    // ── Sessions ───────────────────────────────────────────────────────────

    fun createSession(campaignId: String, masterId: String, date: Long): Session = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can create sessions")

        val newId = UUID.randomUUID().toString()
        Sessions.insert {
            it[id] = newId
            it[Sessions.campaignId] = campaignId
            it[Sessions.date] = date
            it[status] = SessionStatus.PLANNED.name
        }
        getSession(campaignId, newId)
    }

    fun getSessions(campaignId: String): List<Session> = transaction {
        getCampaign(campaignId) // validates existence
        Sessions.selectAll().where { Sessions.campaignId eq campaignId }
            .map { it.toSession() }
    }

    fun getSession(campaignId: String, sessionId: String): Session = transaction {
        Sessions.selectAll().where { (Sessions.campaignId eq campaignId) and (Sessions.id eq sessionId) }
            .map { it.toSession() }
            .singleOrNull() ?: throw NoSuchElementException("Session not found")
    }

    fun updateSessionStatus(
        campaignId: String, sessionId: String, masterId: String, status: SessionStatus
    ): Session = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can update session status")

        Sessions.update({ (Sessions.campaignId eq campaignId) and (Sessions.id eq sessionId) }) {
            it[Sessions.status] = status.name
        }
        getSession(campaignId, sessionId)
    }

    fun updateSessionNotes(
        campaignId: String, sessionId: String, masterId: String, notes: String
    ): Session = transaction {
        val campaign = getCampaign(campaignId)
        if (campaign.masterId != masterId)
            throw IllegalAccessException("Only the master can update session notes")

        Sessions.update({ (Sessions.campaignId eq campaignId) and (Sessions.id eq sessionId) }) {
            it[rawNotes] = notes
        }
        getSession(campaignId, sessionId)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    fun isMember(campaignId: String, userId: String): Boolean = transaction {
        CampaignMembers.selectAll().where {
            (CampaignMembers.campaignId eq campaignId) and (CampaignMembers.userId eq userId)
        }.count() > 0
    }

    fun isMaster(campaignId: String, userId: String): Boolean = transaction {
        Campaigns.selectAll().where {
            (Campaigns.id eq campaignId) and (Campaigns.masterId eq userId)
        }.count() > 0
    }
}

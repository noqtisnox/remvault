package dev.remvault.server.database

import org.jetbrains.exposed.sql.Table

object Campaigns : Table("campaigns") {
    val id = varchar("id", 36)
    val name = varchar("name", 100)
    val masterId = varchar("master_id", 36).references(Users.id)
    val description = text("description").nullable()
    val setting = varchar("setting", 100).nullable()
    val status = varchar("status", 50)

    override val primaryKey = PrimaryKey(id)
}

object CampaignMembers : Table("campaign_members") {
    val userId = varchar("user_id", 36).references(Users.id)
    val campaignId = varchar("campaign_id", 36).references(Campaigns.id)
    val characterId = varchar("character_id", 36).nullable()
    val joinedAt = long("joined_at")

    override val primaryKey = PrimaryKey(userId, campaignId)
}

object Sessions : Table("sessions") {
    val id = varchar("id", 36)
    val campaignId = varchar("campaign_id", 36).references(Campaigns.id)
    val date = long("date")
    val status = varchar("status", 50)
    val rawNotes = text("raw_notes").nullable()
    val aiSummary = text("ai_summary").nullable()

    override val primaryKey = PrimaryKey(id)
}
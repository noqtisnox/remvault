package dev.remvault.server.database

import org.jetbrains.exposed.sql.Table

object Characters : Table("characters") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val campaignId = varchar("campaign_id", 36).references(Campaigns.id).nullable()
    val name = varchar("name", 100)
    val race = varchar("race", 50)
    val subrace = varchar("subrace", 50).nullable()
    val characterClass = varchar("character_class", 50)
    val subclass = varchar("subclass", 50).nullable()
    val level = integer("level").default(1)
    val background = varchar("background", 100)
    val alignment = varchar("alignment", 50).nullable()
    val experiencePoints = integer("experience_points").default(0)
    val inspiration = bool("inspiration").default(false)
    val status = varchar("status", 50)

    override val primaryKey = PrimaryKey(id)
}

object CharacterStats : Table("character_stats") {
    val characterId = varchar("character_id", 36).references(Characters.id)
    val strength = integer("strength")
    val dexterity = integer("dexterity")
    val constitution = integer("constitution")
    val intelligence = integer("intelligence")
    val wisdom = integer("wisdom")
    val charisma = integer("charisma")

    override val primaryKey = PrimaryKey(characterId)
}

object CharacterHitPoints : Table("character_hit_points") {
    val characterId = varchar("character_id", 36).references(Characters.id)
    val maximum = integer("maximum")
    val current = integer("current")
    val temporary = integer("temporary").default(0)
    val hitDiceTotal = integer("hit_dice_total")
    val hitDiceUsed = integer("hit_dice_used").default(0)

    override val primaryKey = PrimaryKey(characterId)
}

object CharacterDeathSaves : Table("character_death_saves") {
    val characterId = varchar("character_id", 36).references(Characters.id)
    val successes = integer("successes").default(0)
    val failures = integer("failures").default(0)

    override val primaryKey = PrimaryKey(characterId)
}

object CharacterSkills : Table("character_skills") {
    val characterId = varchar("character_id", 36).references(Characters.id)
    val skillName = varchar("skill_name", 50)
    val stat = varchar("stat", 20)
    val proficiency = varchar("proficiency", 50)

    override val primaryKey = PrimaryKey(characterId, skillName) // Composite Key
}

object CharacterSpellSlots : Table("character_spell_slots") {
    val characterId = varchar("character_id", 36).references(Characters.id)
    val level = integer("level")
    val maximum = integer("maximum")
    val current = integer("current")

    override val primaryKey = PrimaryKey(characterId, level) // Composite Key
}

object CharacterInventory : Table("character_inventory") {
    val id = varchar("id", 36)
    val characterId = varchar("character_id", 36).references(Characters.id)
    val itemName = varchar("item_name", 100)
    val quantity = integer("quantity").default(1)
    val equipped = bool("equipped").default(false)
    val attuned = bool("attuned").default(false)
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
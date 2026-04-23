package dev.testutils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.remvault.server.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object TestDatabase {
    private var initialized = false

    fun init() {
        if (initialized) {
            dropAll()
        }

        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
            username = "sa"
            password = ""
            maximumPoolSize = 2
            isAutoCommit = false
        }

        // Enable mock Redis for tests
        System.setProperty("redis.mock", "true")

        val ds = HikariDataSource(config)
        Database.connect(ds)

        transaction {
            SchemaUtils.create(
                Users,
                Campaigns,
                Characters,
                CampaignMembers,
                Sessions,
                CharacterStats,
                CharacterHitPoints,
                CharacterDeathSaves,
                CharacterSkills,
                CharacterSpellSlots,
                CharacterInventory
            )
        }

        initialized = true
    }

    fun dropAll() {
        if (!initialized) return
        transaction {
            SchemaUtils.drop(
                CharacterInventory,
                CharacterSpellSlots,
                CharacterSkills,
                CharacterDeathSaves,
                CharacterHitPoints,
                CharacterStats,
                Sessions,
                CampaignMembers,
                Characters,
                Campaigns,
                Users
            )
        }
        initialized = false
    }
}

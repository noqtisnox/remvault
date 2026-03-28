import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.remvault.server.database.CampaignMembers
import dev.remvault.server.database.Campaigns
import dev.remvault.server.database.CharacterDeathSaves
import dev.remvault.server.database.CharacterHitPoints
import dev.remvault.server.database.CharacterInventory
import dev.remvault.server.database.CharacterSkills
import dev.remvault.server.database.CharacterSpellSlots
import dev.remvault.server.database.CharacterStats
import dev.remvault.server.database.Characters
import dev.remvault.server.database.Sessions
import dev.remvault.server.database.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val config = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://localhost:5432/remvault"
        username = "remvault"
        password = "remvault"
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

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

    // Optional: Log that it connected successfully
    log.info("Successfully connected to the PostgreSQL database.")
}

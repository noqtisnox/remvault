package dev.remvault.server.database

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
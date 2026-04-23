package dev.remvault.server.services

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object RedisService {
    private val useMock: Boolean = (System.getProperty("redis.mock") ?: "false").toBoolean()

    private data class Entry(val value: String, val expiresAt: Long)

    // In-memory mock store for tests
    private val mockStore: ConcurrentHashMap<String, Entry>? = if (useMock) ConcurrentHashMap() else null

    private val redisHost = System.getenv("REDIS_HOST") ?: "127.0.0.1"
    private val pool: JedisPool? = if (!useMock) JedisPool(JedisPoolConfig(), redisHost, 6379) else null

    fun get(key: String): String? {
        if (useMock) {
            val e = mockStore?.get(key) ?: return null
            if (e.expiresAt > 0 && System.currentTimeMillis() > e.expiresAt) {
                mockStore.remove(key)
                return null
            }
            return e.value
        }

        pool!!.resource.use { jedis ->
            return jedis.get(key)
        }
    }

    fun set(key: String, value: String, expireSeconds: Long = 3600) {
        if (useMock) {
            val expiresAt = if (expireSeconds > 0) System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds) else 0L
            mockStore?.put(key, Entry(value, expiresAt))
            return
        }

        pool!!.resource.use { jedis ->
            jedis.setex(key, expireSeconds, value)
        }
    }

    fun delete(key: String) {
        if (useMock) {
            mockStore?.remove(key)
            return
        }

        pool!!.resource.use { jedis ->
            jedis.del(key)
        }
    }
}
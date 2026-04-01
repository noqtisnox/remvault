package dev.remvault.server.services

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisService {
    val redisHost = System.getenv("REDIS_HOST") ?: "127.0.0.1"
    private val pool = JedisPool(JedisPoolConfig(), redisHost, 6379)

    fun get(key: String): String? {
        pool.resource.use { jedis -> 
            return jedis.get(key) 
        }
    }

    fun set(key: String, value: String, expireSeconds: Long = 3600) {
        pool.resource.use { jedis -> 
            jedis.setex(key, expireSeconds, value) 
        }
    }

    fun delete(key: String) {
        pool.resource.use { jedis -> 
            jedis.del(key) 
        }
    }
}
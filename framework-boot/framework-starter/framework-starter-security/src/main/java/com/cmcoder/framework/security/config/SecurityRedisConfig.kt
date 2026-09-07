package com.lumispring.framework.security.config

import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * Security 模块 Redis 配置类
 *
 * 支持两种模式：
 * 1. 共用模式：不配置 security.redis，使用默认的 spring.redis
 * 2. 独立模式：配置 security.redis，使用独立的 Redis 连接
 *
 * 支持：单机、集群、哨兵模式
 *
 * 参考 SecurityDataSourceConfig 的兜底策略实现
 */
@Configuration
class SecurityRedisConfig {

    /**
     * Security 独立 Redis 连接工厂（基于 Redisson）
     * 仅在配置了 security.redis.host 时才创建
     */
    @Bean(name = ["securityRedisConnectionFactory"])
    @ConditionalOnProperty(
        prefix = "security.redis",
        name = ["host"],
        matchIfMissing = false
    )
    fun securityRedisConnectionFactory(properties: SecurityProperties): RedisConnectionFactory {
        val redisConfig = properties.redis!!
        val config = Config()

        // 根据配置类型创建不同的连接模式
        when {
            // 集群模式
            redisConfig.cluster?.nodes?.isNotEmpty() == true -> {
                val clusterConfig = config.useClusterServers()
                clusterConfig.addNodeAddress(*redisConfig.cluster.nodes.map { "redis://$it" }.toTypedArray())
                redisConfig.password?.let { if (it.isNotBlank()) clusterConfig.setPassword(it) }
                clusterConfig.setRetryAttempts(3)
                clusterConfig.setRetryInterval(1000)
            }
            // 哨兵模式
            redisConfig.sentinel?.nodes?.isNotEmpty() == true -> {
                val sentinelConfig = config.useSentinelServers()
                sentinelConfig.masterName = redisConfig.sentinel.master
                sentinelConfig.addSentinelAddress(*redisConfig.sentinel.nodes.map { "redis://$it" }.toTypedArray())
                redisConfig.password?.let { if (it.isNotBlank()) sentinelConfig.setPassword(it) }
                redisConfig.sentinel.password?.let { if (it.isNotBlank()) sentinelConfig.setSentinelPassword(it) }
                sentinelConfig.database = redisConfig.database
            }
            // 单机模式（默认）
            else -> {
                val singleConfig = config.useSingleServer()
                singleConfig.address = "redis://${redisConfig.host}:${redisConfig.port}"
                singleConfig.database = redisConfig.database
                redisConfig.password?.let { if (it.isNotBlank()) singleConfig.setPassword(it) }
            }
        }

        return RedissonConnectionFactory(Redisson.create(config))
    }

    /**
     * Security 专用 RedisTemplate
     * 如果有独立的 security Redis 则使用，否则使用默认的 RedisTemplate
     */
    @Bean(name = ["securityRedisTemplate"])
    fun securityRedisTemplate(
        @Qualifier("securityRedisConnectionFactory") securityFactory: RedisConnectionFactory?,
        @Qualifier("stringRedisTemplate") redisTemplate: StringRedisTemplate
    ): RedisTemplate<String, String> {
        // 如果没有配置独立 Redis，返回默认的 RedisTemplate
        if (securityFactory == null) {
            return redisTemplate
        }

        // 创建独立的 RedisTemplate
        return StringRedisTemplate().apply {
            connectionFactory = securityFactory
            afterPropertiesSet()
        }
    }
}

package com.lumispring.framework.database.redis.config

import com.lumispring.framework.base.extension.*
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer


@Configuration
class RedisTemplateConfiguration {

    @Autowired
    lateinit var redissonClient: RedissonClient

    @Bean("IRedisTemplate")
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any>{
        return RedisTemplate<String, Any>().apply {
            // 1.设置redis连接工厂
            connectionFactory = factory
            // 2.设置key序列化方式，默认是jdk序列化
            keySerializer = keySerializer()
            hashKeySerializer = keySerializer()
            // 3.设置value序列化方式，默认是jdk序列化
            valueSerializer = valueSerializer()
            hashValueSerializer = valueSerializer()
            // 4.设置事务支持
            setEnableTransactionSupport(true)
            // 5.初始化
            afterPropertiesSet()
        }
    }

    /**
     * key序列化器
     */
    private fun keySerializer():RedisSerializer<String> = RedisSerializer.string()

    /**
     * value序列化器
     */
    private fun valueSerializer():RedisSerializer<Any>{
        return object : RedisSerializer<Any> {
            override fun serialize(t: Any?): ByteArray {
                if (t is String) return t.toByteArray()
                return t?.toJsonString()?.toByteArray() ?: ByteArray(0)
            }
            override fun deserialize(bytes: ByteArray?): Any {
                if (bytes.isNullOrEmpty()) return ""
                return String(bytes).parseJson<Any>() ?: ""
            }
        }
    }
}
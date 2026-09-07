package com.lumispring.framework.security.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("security")
class SecurityProperties {

    /**
     * 是否启用安全模块，包括用户登录、权限校验等
     */
    var enabled: Boolean = true

    /**
     * 登录Token的有效期，7天
     */
    var timeout: Long = 7 * 24 * 60 * 60

    /**
     * API密钥，配合apiHeader一起使用，通过API密钥可以忽略用户验证
     */
    var apiKey: String? = null

    /**
     * API请求头，当请求头中传入 apiHeader: apiKey的时候可以跳过用户验证
     */
    var apiHeader: String = "security-key"

    /**
     * Redis 配置（可选）
     * 如果不配置，则使用默认的 spring.data.redis
     * 如果配置，则使用独立的 Redis 连接
     */
    var redis: RedisProperties? = null

}
package com.lumispring.framework.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("security")
class SecurityProperties {

    /**
     * 是否启用安全模块，包括鉴权注解与当前用户解析
     */
    var enabled: Boolean = true

    /**
     * 登录 Token 的有效期（秒），默认 7 天。由 RBAC 实现使用。
     */
    var timeout: Long = 7 * 24 * 60 * 60

    /**
     * API 密钥，配合 apiHeader 一起使用，通过 API 密钥可以忽略用户验证
     */
    var apiKey: String? = null

    /**
     * API 请求头，当请求头中传入 apiHeader: apiKey 的时候可以跳过用户验证
     */
    var apiHeader: String = "security-key"

}

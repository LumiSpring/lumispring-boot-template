package com.lumispring.framework.security.model.vo

/**
 * Token视图对象
 */
data class TokenVO(
    /**
     * 访问令牌
     */
    val accessToken: String,

    /**
     * 令牌类型
     */
    val tokenType: String = "Bearer",

    /**
     * 过期时间（秒）
     */
    val expiresIn: Long,

    /**
     * 用户信息
     */
    val userInfo: UserVO? = null
)

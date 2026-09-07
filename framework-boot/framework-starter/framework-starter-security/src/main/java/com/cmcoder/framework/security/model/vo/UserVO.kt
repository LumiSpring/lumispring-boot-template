package com.lumispring.framework.security.model.vo

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.lumispring.framework.security.serializer.PhoneMaskSerializer
import java.time.LocalDateTime

/**
 * 用户视图对象
 */
data class UserVO(
    /**
     * 用户ID
     */
    val id: Long? = null,

    /**
     * 用户名
     */
    val username: String? = null,

    /**
     * 昵称
     */
    val nickname: String? = null,

    /**
     * 邮箱
     */
    val email: String? = null,

    /**
     * 手机号（已脱敏）
     */
    @JsonSerialize(using = PhoneMaskSerializer::class)
    val phone: String? = null,

    /**
     * 头像URL
     */
    val avatar: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = null,

    /**
     * 用户类型：1-普通用户，2-管理员
     */
    val userType: Int? = null,

    /**
     * 角色列表
     */
    val roles: List<String>? = null,

    /**
     * 令牌
     */
    @JsonIgnore
    var token: String? = null,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createTime: LocalDateTime? = null,

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updateTime: LocalDateTime? = null
) {
    /**
     * 是否启用
     */
    fun isEnabled(): Boolean = status == 1

    /**
     * 是否管理员
     */
    fun isAdmin(): Boolean = userType == 2 || (roles?.contains("admin") == true)
}

package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户信息更新DTO
 */
data class UserUpdateDTO(
    /**
     * 用户ID
     */
    var userId: Long? = null,

    /**
     * 昵称
     */
    @field:Size(max = 64, message = "昵称长度不能超过64个字符")
    val nickname: String? = null,

    /**
     * 邮箱
     */
    @field:Email(message = "邮箱格式不正确")
    val email: String? = null,

    /**
     * 手机号
     */
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String? = null,

    /**
     * 头像URL
     */
    val avatar: String? = null
)

package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 注册请求DTO
 */
data class UserCreateDTO(
    /**
     * 用户名
     */
    @field:NotBlank(message = "用户名不能为空")
    var username: String,

    /**
     * 密码
     */
    @field:NotBlank(message = "密码不能为空")
    var password: String,

    /**
     * 昵称
     */
    var nickname: String? = null,

    /**
     * 邮箱
     */
    var email: String? = null,

    /**
     * 手机号
     */
    var phone: String? = null,

    /**
     * 角色列表
     */
    var roles: List<String>? = null,
)

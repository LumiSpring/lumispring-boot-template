package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * 登录请求DTO
 */
data class LoginDTO(
    /**
     * 登录账号（用户名、邮箱或手机号）
     */
    @field:NotBlank(message = "账号不能为空")
    val username: String,

    /**
     * 密码
     */
    @field:NotBlank(message = "密码不能为空")
    val password: String
)

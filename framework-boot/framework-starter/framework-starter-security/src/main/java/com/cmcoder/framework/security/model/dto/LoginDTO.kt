package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * 登录请求DTO
 */
data class LoginDTO(
    /**
     * 用户名
     */
    @field:NotBlank(message = "用户名不能为空")
    val username: String,

    /**
     * 密码
     */
    @field:NotBlank(message = "密码不能为空")
    val password: String
)

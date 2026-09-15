package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 管理员重置用户密码
 */
data class PasswordResetDTO(
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    val newPassword: String,

    @field:NotBlank(message = "确认密码不能为空")
    val confirmPassword: String
)

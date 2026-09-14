package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 密码修改DTO
 */
data class PasswordUpdateDTO(
    var userId: Long? = null,

    /**
     * 旧密码
     */
    @field:NotBlank(message = "旧密码不能为空")
    val oldPassword: String,

    /**
     * 新密码
     */
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    val newPassword: String,

    /**
     * 确认新密码
     */
    @field:NotBlank(message = "确认密码不能为空")
    val confirmPassword: String
)

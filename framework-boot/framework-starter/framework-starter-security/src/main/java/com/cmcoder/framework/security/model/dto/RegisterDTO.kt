package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 注册请求DTO
 */
data class RegisterDTO(
    /**
     * 用户名
     */
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,

    /**
     * 密码
     */
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    val password: String,

    /**
     * 确认密码
     */
    @field:NotBlank(message = "确认密码不能为空")
    val confirmPassword: String,

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
    val phone: String? = null
)

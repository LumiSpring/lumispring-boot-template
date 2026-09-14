package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 角色创建DTO
 */
data class RoleDTO(
    /**
     * 角色ID（更新时使用）
     */
    val id: Long? = null,

    /**
     * 角色名称
     */
    @field:NotBlank(message = "角色名称不能为空")
    @field:Size(max = 64, message = "角色名称长度不能超过64个字符")
    val roleName: String,

    /**
     * 角色编码
     */
    @field:NotBlank(message = "角色编码不能为空")
    @field:Size(max = 64, message = "角色编码长度不能超过64个字符")
    val roleCode: String,

    /**
     * 描述
     */
    @field:Size(max = 255, message = "描述长度不能超过255个字符")
    val description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = 1
)

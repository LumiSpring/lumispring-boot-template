package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 角色更新DTO
 */
data class RoleUpdateDTO(
    /**
     * 角色ID（更新时使用）
     */
    val id: Long? = null,

    /**
     * 角色名称
     */
    val roleName: String? = null,

    /**
     * 角色编码
     */
    val roleCode: String? = null,

    /**
     * 描述
     */
    val description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = 1
)

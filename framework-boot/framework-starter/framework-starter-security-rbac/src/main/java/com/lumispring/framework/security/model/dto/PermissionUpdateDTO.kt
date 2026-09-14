package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.Size

/**
 * 权限更新DTO
 */
data class PermissionUpdateDTO(
    /**
     * 权限名称
     */
    @field:Size(max = 64, message = "权限名称长度不能超过64个字符")
    val name: String? = null,

    /**
     * 权限编码：【模块:资源:名称】
     */
    @field:Size(max = 64, message = "权限编码长度不能超过64个字符")
    val code: String? = null,

    /**
     * 权限路径：路由/API
     */
    @field:Size(max = 255, message = "权限路径长度不能超过255个字符")
    val path: String? = null,

    /**
     * 父级权限ID
     */
    val parentId: Long? = null,

    /**
     * 权限类型：api-接口、menu-菜单、button-按钮
     */
    val type: String? = null,

    /**
     * 组件路径
     */
    @field:Size(max = 255, message = "组件路径长度不能超过255个字符")
    val component: String? = null,

    /**
     * 描述
     */
    @field:Size(max = 255, message = "描述长度不能超过255个字符")
    val description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = null
)

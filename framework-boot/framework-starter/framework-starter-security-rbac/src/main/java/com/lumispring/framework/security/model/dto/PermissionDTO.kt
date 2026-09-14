package com.lumispring.framework.security.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 权限创建DTO
 */
data class PermissionDTO(
    /**
     * 权限名称
     */
    @field:NotBlank(message = "权限名称不能为空")
    @field:Size(max = 64, message = "权限名称长度不能超过64个字符")
    val name: String,

    /**
     * 权限编码：【模块:资源:名称】
     */
    @field:NotBlank(message = "权限编码不能为空")
    @field:Size(max = 64, message = "权限编码长度不能超过64个字符")
    val code: String,

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
    @field:NotBlank(message = "权限类型不能为空")
    val type: String,

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
    val status: Int? = 1
)

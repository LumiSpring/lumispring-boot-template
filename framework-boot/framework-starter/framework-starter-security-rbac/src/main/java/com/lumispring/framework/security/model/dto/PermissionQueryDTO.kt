package com.lumispring.framework.security.model.dto

/**
 * 权限查询DTO
 */
data class PermissionQueryDTO(
    /**
     * 权限名称（模糊查询）
     */
    var name: String? = null,

    /**
     * 权限编码（模糊查询）
     */
    var code: String? = null,

    /**
     * 权限类型：api-接口、menu-菜单、button-按钮
     */
    var type: String? = null,

    /**
     * 父级权限ID
     */
    var parentId: Long? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    var status: Int? = null
)

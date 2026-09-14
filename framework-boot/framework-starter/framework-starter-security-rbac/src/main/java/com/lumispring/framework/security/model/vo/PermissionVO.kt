package com.lumispring.framework.security.model.vo

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * 权限视图对象
 */
data class PermissionVO(
    /**
     * 权限ID
     */
    val id: Long? = null,

    /**
     * 权限名称
     */
    val name: String? = null,

    /**
     * 权限编码
     */
    val code: String? = null,

    /**
     * 权限路径：路由/API
     */
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
    val component: String? = null,

    /**
     * 描述
     */
    val description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = null,

    /**
     * 子权限列表（树形结构时使用）
     */
    val children: List<PermissionVO>? = null,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createTime: LocalDateTime? = null,

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updateTime: LocalDateTime? = null
) {
    /**
     * 是否启用
     */
    fun isEnabled(): Boolean = status == 1
}

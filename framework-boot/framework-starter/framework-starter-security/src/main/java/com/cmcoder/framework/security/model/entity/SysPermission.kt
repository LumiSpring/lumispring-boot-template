package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.*
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime

/**
 * 权限实体类
 */
@TableName("sys_permission")
data class SysPermission(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 权限名称
     */
    var name: String? = null,

    /**
     * 权限编码：【模块:资源:名称】
     */
    var code: String? = null,

    /**
     * 权限路径：路由/API
     */
    var path: String? = null,

    /**
     * 父级权限ID
     */
    @TableField("parent_id")
    var parentId: Long? = null,

    /**
     * 权限类型：api-接口、menu-菜单、button-按钮
     */
    var type: String? = null,

    /**
     * 组件路径
     */
    var component: String? = null,

    /**
     * 描述
     */
    var description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    var status: Int? = 1,

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    var createTime: LocalDateTime? = null,

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    var updateTime: LocalDateTime? = null,

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    var createBy: Long? = null,

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    var updateBy: Long? = null
) : Model<SysPermission>() {
    companion object {
        const val STATUS_ENABLED = 1
        const val STATUS_DISABLED = 0

        // 权限类型
        const val TYPE_API = "api"
        const val TYPE_MENU = "menu"
        const val TYPE_BUTTON = "button"
    }

    /**
     * 是否启用
     */
    fun isEnabled(): Boolean = status == STATUS_ENABLED
}

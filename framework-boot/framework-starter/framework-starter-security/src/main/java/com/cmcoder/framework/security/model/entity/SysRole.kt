package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.*
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime

/**
 * 角色实体类
 */
@TableName("sys_role")
data class SysRole(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 角色名称
     */
    @TableField("role_name")
    var roleName: String? = null,

    /**
     * 角色编码
     */
    @TableField("role_code")
    var roleCode: String? = null,

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
    var updateTime: LocalDateTime? = null
) : Model<SysRole>() {
    companion object {
        const val STATUS_ENABLED = 1
        const val STATUS_DISABLED = 0

        // 内置角色
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
    }

    /**
     * 是否启用
     */
    fun isEnabled(): Boolean = status == STATUS_ENABLED
}

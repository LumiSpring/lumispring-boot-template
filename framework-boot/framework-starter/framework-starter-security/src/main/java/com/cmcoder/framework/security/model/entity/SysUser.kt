package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.*
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime

/**
 * 用户实体类
 */
@TableName("sys_user")
data class SysUser(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 用户名
     */
    var username: String? = null,

    /**
     * 密码
     */
    var password: String? = null,

    /**
     * 昵称
     */
    var nickname: String? = null,

    /**
     * 邮箱
     */
    var email: String? = null,

    /**
     * 手机号
     */
    var phone: String? = null,

    /**
     * 头像URL
     */
    var avatar: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    var status: Int? = 1,

    /**
     * 用户类型：1-普通用户，2-管理员
     */
    @TableField("user_type")
    var userType: Int? = 1,

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
) : Model<SysUser>() {
    companion object {
        // 状态：1-启用，0-禁用
        const val STATUS_ENABLED = 1
        const val STATUS_DISABLED = 0

        // 用户类型：1-普通用户，2-管理员
        const val TYPE_NORMAL = 1
        const val TYPE_ADMIN = 2
    }

    /**
     * 是否启用
     */
    fun isEnabled(): Boolean = status == STATUS_ENABLED

    /**
     * 是否管理员
     */
    fun isAdmin(): Boolean = userType == TYPE_ADMIN
}

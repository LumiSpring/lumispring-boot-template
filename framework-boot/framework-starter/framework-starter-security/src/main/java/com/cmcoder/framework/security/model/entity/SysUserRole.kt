package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime

/**
 * 用户角色关联实体
 */
@TableName("sys_user_role")
data class SysUserRole(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 用户ID
     */
    @TableField("user_id")
    var userId: Long? = null,

    /**
     * 角色ID
     */
    @TableField("role_id")
    var roleId: Long? = null,

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    var createTime: LocalDateTime? = null
): Model<SysUserRole>()

package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model

/**
 * 用户权限关联实体
 */
@TableName("sys_user_permission")
data class SysUserPermission(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 用户ID
     */
    @TableField("user_id")
    var userId: Long? = null,

    /**
     * 权限ID
     */
    @TableField("permission_id")
    var permissionId: Long? = null
) : Model<SysUserPermission>()

package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model

/**
 * 角色权限关联实体
 */
@TableName("sys_role_permission")
data class SysRolePermission(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 角色ID
     */
    @TableField("role_id")
    var roleId: Long? = null,

    /**
     * 权限ID
     */
    @TableField("permission_id")
    var permissionId: Long? = null
) : Model<SysRolePermission>()

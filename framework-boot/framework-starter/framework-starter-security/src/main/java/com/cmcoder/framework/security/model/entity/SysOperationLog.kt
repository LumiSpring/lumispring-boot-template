package com.lumispring.framework.security.model.entity

import com.baomidou.mybatisplus.annotation.*
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime

/**
 * 操作日志实体类
 */
@TableName("sys_operation_log")
data class SysOperationLog(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    var operatorId: Long? = null,

    /**
     * 操作人名称
     */
    @TableField("operator_name")
    var operatorName: String? = null,

    /**
     * 模块：user/role/permission
     */
    var module: String? = null,

    /**
     * 操作类型：create/update/delete
     */
    var action: String? = null,

    /**
     * 目标数据ID
     */
    @TableField("target_id")
    var targetId: Long? = null,

    /**
     * 目标数据名称
     */
    @TableField("target_name")
    var targetName: String? = null,

    /**
     * 操作数据快照（JSON格式）
     */
    var detail: String? = null,

    /**
     * 操作IP
     */
    var ip: String? = null,

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    var createTime: LocalDateTime? = null
) : Model<SysOperationLog>() {
    companion object {
        // 模块常量
        const val MODULE_USER = "user"
        const val MODULE_ROLE = "role"
        const val MODULE_PERMISSION = "permission"

        // 操作类型常量
        const val ACTION_CREATE = "create"
        const val ACTION_UPDATE = "update"
        const val ACTION_DELETE = "delete"
    }
}

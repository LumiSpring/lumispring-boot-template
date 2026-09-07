package com.lumispring.framework.security.model.vo

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * 角色视图对象
 */
data class RoleVO(
    /**
     * 角色ID
     */
    val id: Long? = null,

    /**
     * 角色名称
     */
    val roleName: String? = null,

    /**
     * 角色编码
     */
    val roleCode: String? = null,

    /**
     * 描述
     */
    val description: String? = null,

    /**
     * 状态：0-禁用，1-启用
     */
    val status: Int? = null,

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

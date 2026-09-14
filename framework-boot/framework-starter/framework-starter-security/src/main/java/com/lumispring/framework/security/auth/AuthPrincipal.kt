package com.lumispring.framework.security.auth

import kotlin.reflect.KClass

/**
 * 当前登录主体。
 *
 * 内核只使用身份与权限字段做注解校验，不绑定任何用户表。
 * 业务自己的用户对象放在 [userDetails]，再用 [asUser] 按类型取出。
 */
interface AuthPrincipal {
    /**
     * 用户唯一标识，由业务决定格式（数字 ID、UUID 等均可）。
     */
    val id: String

    /**
     * 登录名，可为空。
     */
    val username: String?

    /**
     * 当前用户拥有的角色编码，供 [@com.lumispring.framework.security.config.annotation.RequireRole] 使用。
     */
    val roles: Set<String>

    /**
     * 当前用户拥有的权限编码。内核暂未提供对应注解，留给业务扩展。
     */
    val permissions: Set<String>

    /**
     * 是否管理员，供 [@com.lumispring.framework.security.config.annotation.RequireAdmin] 使用。
     */
    val admin: Boolean

    /**
     * 业务侧的用户对象，例如项目自己的 UserVO。
     * 内核不会读取该字段，只负责随请求上下文传递。
     */
    val userDetails: Any?

    /**
     * 将 [userDetails] 转为指定的业务用户类型。
     *
     * 类型不匹配或尚未登录时返回 `null`，不会抛出转换异常。
     *
     * @param type 业务用户类型，如 `MyUserVO::class`
     */
    fun <T : Any> asUser(type: KClass<T>): T? {
        val value = userDetails ?: return null
        return if (type.isInstance(value)) type.java.cast(value) else null
    }
}

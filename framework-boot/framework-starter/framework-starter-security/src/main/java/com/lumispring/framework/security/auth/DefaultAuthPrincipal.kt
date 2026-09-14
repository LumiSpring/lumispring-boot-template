package com.lumispring.framework.security.auth

/**
 * [AuthPrincipal] 的默认实现。
 *
 * @param id 用户唯一标识
 * @param username 登录名
 * @param roles 角色编码
 * @param permissions 权限编码
 * @param admin 是否管理员
 * @param userDetails 业务用户对象，可通过 [AuthPrincipal.asUser] 按类型取出
 */
data class DefaultAuthPrincipal(
    override val id: String,
    override val username: String? = null,
    override val roles: Set<String> = emptySet(),
    override val permissions: Set<String> = emptySet(),
    override val admin: Boolean = false,
    override val userDetails: Any? = null
) : AuthPrincipal

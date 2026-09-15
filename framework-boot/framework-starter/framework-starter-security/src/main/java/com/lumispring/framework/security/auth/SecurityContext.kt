package com.lumispring.framework.security.auth

import com.alibaba.ttl.TransmittableThreadLocal

/**
 * 当前请求的登录主体上下文，基于线程传递，请求结束时必须 [clear]。
 */
object SecurityContext {
    private val holder = TransmittableThreadLocal<AuthPrincipal>()

    fun get(): AuthPrincipal? = holder.get()

    fun set(principal: AuthPrincipal?) {
        if (principal == null) {
            holder.remove()
        } else {
            holder.set(principal)
        }
    }

    fun clear() = holder.remove()
}

/** 当前登录主体，未登录时为 `null`。 */
fun currentPrincipal(): AuthPrincipal? = SecurityContext.get()

/** 当前用户 ID（字符串形式，由 [AuthenticationResolver] 写入）。 */
fun currentUserId(): String? = currentPrincipal()?.id

fun currentUsername(): String? = currentPrincipal()?.username

fun currentRoles(): Set<String> = currentPrincipal()?.roles.orEmpty()

fun currentPermissions(): Set<String> = currentPrincipal()?.permissions.orEmpty()

fun isAdmin(): Boolean = currentPrincipal()?.admin == true

/**
 * 取出当前请求中的业务用户对象。
 *
 * ```kotlin
 * val user = currentUser<MyUserVO>()
 * ```
 *
 * 未登录或 [AuthPrincipal.userDetails] 不是 [T] 时返回 `null`。
 */
inline fun <reified T : Any> currentUser(): T? = currentPrincipal()?.asUser(T::class)

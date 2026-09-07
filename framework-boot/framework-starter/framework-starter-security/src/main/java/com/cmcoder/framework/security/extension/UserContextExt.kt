package com.lumispring.framework.security.extension

import com.alibaba.ttl.TransmittableThreadLocal
import com.lumispring.framework.base.extension.getBeanOrNull
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService


private val userThreadLocal = TransmittableThreadLocal<UserVO>()

private val userService by lazy {
    getBeanOrNull(UserService::class.java)
}

fun currentUser(): UserVO? = userThreadLocal.get()

fun setCurrentUser(user: UserVO?) = userThreadLocal.set(user)

fun removeCurrentUser() = userThreadLocal.remove()

fun currentUsername(): String? = currentUser()?.username

fun currentUserId(): Long? = currentUser()?.id

fun currentUserRoles(): List<String>? = currentUser()?.roles

fun currentToken(): String? = currentUser()?.token

/**
 * 是否是管理员用户
 */
fun isAdmin(): Boolean = currentUser()?.isAdmin() == true


/**
 * 当前用户上下文持有者（供其他模块通过反射获取）
 */
object CurrentUserContext {
    @JvmStatic
    fun getCurrentUser(): UserVO? = currentUser()

    @JvmStatic
    fun getCurrentUserId(): Long? = currentUser()?.id

    @JvmStatic
    fun getCurrentUsername(): String? = currentUser()?.username

    @JvmStatic
    fun getCurrentUserRoles(): List<String>? = currentUserRoles()

    @JvmStatic
    fun getCurrentToken(): String? = currentToken()

    @JvmStatic
    fun isAdmin(): Boolean = currentUser()?.isAdmin() == true
}




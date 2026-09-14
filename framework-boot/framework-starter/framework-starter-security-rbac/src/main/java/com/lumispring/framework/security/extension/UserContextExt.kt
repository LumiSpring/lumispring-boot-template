package com.lumispring.framework.security.extension

import com.lumispring.framework.security.auth.DefaultAuthPrincipal
import com.lumispring.framework.security.auth.SecurityContext
import com.lumispring.framework.security.auth.currentPrincipal
import com.lumispring.framework.security.auth.currentUser as currentUserDetails
import com.lumispring.framework.security.model.vo.UserVO

fun currentUser(): UserVO? = currentUserDetails<UserVO>()

fun setCurrentUser(user: UserVO?) {
    if (user == null) {
        SecurityContext.clear()
        return
    }
    val userId = user.id?.toString() ?: return
    SecurityContext.set(
        DefaultAuthPrincipal(
            id = userId,
            username = user.username,
            roles = user.roles.orEmpty().toSet(),
            permissions = emptySet(),
            admin = user.isAdmin(),
            userDetails = user
        )
    )
}

fun removeCurrentUser() = SecurityContext.clear()

fun currentUsername(): String? = currentUser()?.username

fun currentUserId(): Long? = currentUser()?.id

fun currentUserRoles(): List<String>? = currentUser()?.roles

fun currentToken(): String? = currentUser()?.token

fun isAdmin(): Boolean = currentPrincipal()?.admin == true

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
    fun isAdmin(): Boolean = currentPrincipal()?.admin == true
}

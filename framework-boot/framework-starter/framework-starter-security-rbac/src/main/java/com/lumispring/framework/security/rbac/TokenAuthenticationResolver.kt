package com.lumispring.framework.security.rbac

import com.lumispring.framework.database.redis.core.RedisClient
import com.lumispring.framework.security.auth.AuthPrincipal
import com.lumispring.framework.security.auth.AuthenticationResolver
import com.lumispring.framework.security.auth.DefaultAuthPrincipal
import com.lumispring.framework.security.config.SecurityRedisKeyConst
import com.lumispring.framework.security.model.vo.UserVO
import jakarta.servlet.http.HttpServletRequest

/**
 * 默认实现：从 Authorization Bearer 或 Cookie token 读取 Redis 中的 UserVO。
 */
class TokenAuthenticationResolver : AuthenticationResolver {

    override fun resolve(request: HttpServletRequest): AuthPrincipal? {
        val token = resolveToken(request) ?: return null
        val user = RedisClient.get<UserVO>("${SecurityRedisKeyConst.USER_INFO_BY_TOKEN_PREFIX}:$token") ?: return null
        val userId = user.id?.toString() ?: return null
        user.token = token
        return DefaultAuthPrincipal(
            id = userId,
            username = user.username,
            roles = user.roles.orEmpty().toSet(),
            permissions = emptySet(),
            admin = user.isAdmin(),
            userDetails = user
        )
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        if (!header.isNullOrBlank()) {
            return header.replace("Bearer ", "", ignoreCase = true).trim().takeIf { it.isNotBlank() }
        }
        return request.cookies?.firstOrNull { it.name == "token" }?.value?.takeIf { it.isNotBlank() }
    }
}

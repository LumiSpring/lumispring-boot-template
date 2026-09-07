package com.lumispring.framework.security.config

import com.lumispring.framework.base.extension.toObject
import com.lumispring.framework.security.extension.setCurrentUser
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 安全拦截器
 */
class SecurityInterceptor(
    private val properties: SecurityProperties,
    private val redisTemplate: RedisTemplate<String, String>
) : HandlerInterceptor {

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     * 返回true：放行该请求
     * 返回false：拦截该请求
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        /**
         * Token的获取方式
         * 1. Authorization请求头， Bearer Token
         * 2. Cookie Token
         */
        val token = request.getHeader("Authorization")?.replace("Bearer ", "")
            ?: request.cookies?.firstOrNull { it.name == "token" }?.value

        if (!token.isNullOrBlank()){
            val userVo = redisTemplate.opsForValue().get("${SecurityRedisKeyConst.USER_INFO_BY_TOKEN_PREFIX}:${token}").toObject<UserVO>()
            userVo?.let {
                it.token = token
                setCurrentUser(it)
            }
        }

        return super.preHandle(request, response, handler)
    }
}
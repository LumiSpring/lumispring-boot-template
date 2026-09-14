package com.lumispring.framework.security.config

import com.lumispring.framework.security.auth.AuthenticationResolver
import com.lumispring.framework.security.auth.SecurityContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.ObjectProvider
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 调用 [AuthenticationResolver] 解析当前主体，并在请求结束时清理上下文。
 */
class SecurityInterceptor(
    private val resolvers: ObjectProvider<AuthenticationResolver>
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val resolver = resolvers.getIfAvailable()
        SecurityContext.set(resolver?.resolve(request))
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        SecurityContext.clear()
    }
}

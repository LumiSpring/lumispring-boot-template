package com.lumispring.framework.security.auth

import jakarta.servlet.http.HttpServletRequest

/**
 * 从当前请求解析登录主体。
 *
 * 业务项目提供自己的实现即可替换默认 Token 解析；
 * 解析得到的业务用户对象应放到 [AuthPrincipal.userDetails]。
 */
fun interface AuthenticationResolver {
    /**
     * @return 已登录则返回主体，未登录返回 `null`
     */
    fun resolve(request: HttpServletRequest): AuthPrincipal?
}

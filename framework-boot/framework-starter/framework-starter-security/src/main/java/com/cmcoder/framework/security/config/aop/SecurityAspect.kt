package com.lumispring.framework.security.config.aop

import com.lumispring.framework.base.extension.throwIf
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.config.annotation.RequireLogin
import com.lumispring.framework.security.config.annotation.RequireRole
import com.lumispring.framework.security.config.annotation.UnAuth
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.extension.currentUserRoles
import com.lumispring.framework.security.extension.isAdmin
import com.lumispring.framework.security.service.RoleService
import com.lumispring.framework.web.extension.currentRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

/**
 * 权限校验切面
 */
@Aspect
@Component
class SecurityAspect(
    private val roleService: RoleService,
    private val properties: SecurityProperties,
) {

    /**
     * 处理类级别 @RequireLogin 注解
     */
    @Around("@within(requireLogin)")
    fun aroundClassRequireLogin(joinPoint: ProceedingJoinPoint, requireLogin: RequireLogin): Any? {
        return validatePermission(joinPoint)
    }

    /**
     * 处理方法级别 @RequireLogin 注解
     */
    @Around("@annotation(requireLogin)")
    fun aroundMethodRequireLogin(joinPoint: ProceedingJoinPoint, requireLogin: RequireLogin): Any? {
        return validatePermission(joinPoint)
    }

    /**
     * 处理类级别 @RequireRole 注解
     */
    @Around("@within(requireRole)")
    fun aroundClassRequireRole(joinPoint: ProceedingJoinPoint, requireRole: RequireRole): Any? {
        return validatePermission(joinPoint) {
            validateRole(requireRole)
        }
    }

    /**
     * 处理方法级别 @RequireRole 注解
     */
    @Around("@annotation(requireRole)")
    fun aroundMethodRequireRole(joinPoint: ProceedingJoinPoint, requireRole: RequireRole): Any? {
        return validatePermission(joinPoint) {
            validateRole(requireRole)
        }
    }

    /**
     * 处理类级别 @RequireAdmin 注解
     */
    @Around("@within(requireAdmin)")
    fun aroundClassRequireAdmin(joinPoint: ProceedingJoinPoint, requireAdmin: RequireAdmin): Any? {
        return validatePermission(joinPoint) {
            validateAdmin()
        }
    }

    /**
     * 处理方法级别 @RequireAdmin 注解
     */
    @Around("@annotation(requireAdmin)")
    fun aroundMethodRequireAdmin(joinPoint: ProceedingJoinPoint, requireAdmin: RequireAdmin): Any? {
        return validatePermission(joinPoint) {
            validateAdmin()
        }
    }

    /**
     * 统一的权限校验框架
     * @param joinPoint 切面连接点
     * @param validator 具体的校验逻辑（Lambda）
     */
    private fun validatePermission(joinPoint: ProceedingJoinPoint, validator: () -> Unit = {}): Any? {
        // 1. 检查是否需要跳过验证
        if (hasUnAuthAnnotation(joinPoint) || hasApiHeader(joinPoint)) {
            return joinPoint.proceed()
        }

        // 2. 检查登录状态
        throwIf(currentUserId() == null, errorCode = ErrorCode.USER_NOT_LOGIN_ERROR)

        // 3. 执行具体的校验逻辑
        validator()

        // 4. 继续执行目标方法
        return joinPoint.proceed()
    }


    /**
     * 校验角色
     */
    private fun validateRole(requireRole: RequireRole) {
        val userRoles = currentUserRoles() ?: listOf()
        val requiredRoles = requireRole.value.toList()
        val mode = requireRole.mode

        val hasPermission = when (mode) {
            RequireRole.RoleCheckMode.ANY -> requiredRoles.any { it in userRoles }
            RequireRole.RoleCheckMode.ALL -> requiredRoles.all { it in userRoles }
        }

        if (!hasPermission) {
            throw ErrorCode.AUTH_ERROR.exception("权限不足，需要角色：${requiredRoles.joinToString(", ")}")
        }
    }


    /**
     * 校验管理员
     */
    private fun validateAdmin() {
        throwIf(!isAdmin(), "需要管理员权限", errorCode = ErrorCode.AUTH_ERROR)
    }


    /**
     * 检查方法上是否有 @UnAuth 注解
     */
    private fun hasUnAuthAnnotation(joinPoint: ProceedingJoinPoint): Boolean {
        val method = (joinPoint.signature as MethodSignature).method
        return method.isAnnotationPresent(UnAuth::class.java)
    }


    /**
     * 检查是否用到了apiHeader来跳过验证
     */
    private fun hasApiHeader(joinPoint: ProceedingJoinPoint): Boolean {
        val request = currentRequest()
        val apiKey = request?.getHeader(properties.apiHeader)
        return !properties.apiKey.isNullOrBlank() && apiKey == properties.apiKey
    }
}
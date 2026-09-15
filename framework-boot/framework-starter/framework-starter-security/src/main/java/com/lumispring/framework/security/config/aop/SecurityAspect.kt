package com.lumispring.framework.security.config.aop

import com.lumispring.framework.base.extension.throwIf
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.auth.currentPrincipal
import com.lumispring.framework.security.auth.isAdmin
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.config.annotation.RequireLogin
import com.lumispring.framework.security.config.annotation.RequirePermission
import com.lumispring.framework.security.config.annotation.RequireRole
import com.lumispring.framework.security.config.annotation.UnAuth
import com.lumispring.framework.web.extension.currentRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature

/**
 * 权限校验切面，只依赖 [com.lumispring.framework.security.auth.AuthPrincipal]。
 * 拥有管理员身份时，跳过角色与权限编码校验。
 */
@Aspect
class SecurityAspect(
    private val properties: SecurityProperties
) {

    @Around("@within(requireLogin)")
    fun aroundClassRequireLogin(joinPoint: ProceedingJoinPoint, requireLogin: RequireLogin): Any? {
        return validateAccess(joinPoint)
    }

    @Around("@annotation(requireLogin)")
    fun aroundMethodRequireLogin(joinPoint: ProceedingJoinPoint, requireLogin: RequireLogin): Any? {
        return validateAccess(joinPoint)
    }

    @Around("@within(requireRole)")
    fun aroundClassRequireRole(joinPoint: ProceedingJoinPoint, requireRole: RequireRole): Any? {
        return validateAccess(joinPoint) {
            validateRole(requireRole)
        }
    }

    @Around("@annotation(requireRole)")
    fun aroundMethodRequireRole(joinPoint: ProceedingJoinPoint, requireRole: RequireRole): Any? {
        return validateAccess(joinPoint) {
            validateRole(requireRole)
        }
    }

    @Around("@within(requirePermission)")
    fun aroundClassRequirePermission(joinPoint: ProceedingJoinPoint, requirePermission: RequirePermission): Any? {
        return validateAccess(joinPoint) {
            validatePermissionCodes(requirePermission)
        }
    }

    @Around("@annotation(requirePermission)")
    fun aroundMethodRequirePermission(joinPoint: ProceedingJoinPoint, requirePermission: RequirePermission): Any? {
        return validateAccess(joinPoint) {
            validatePermissionCodes(requirePermission)
        }
    }

    @Around("@within(requireAdmin)")
    fun aroundClassRequireAdmin(joinPoint: ProceedingJoinPoint, requireAdmin: RequireAdmin): Any? {
        return validateAccess(joinPoint) {
            validateAdmin()
        }
    }

    @Around("@annotation(requireAdmin)")
    fun aroundMethodRequireAdmin(joinPoint: ProceedingJoinPoint, requireAdmin: RequireAdmin): Any? {
        return validateAccess(joinPoint) {
            validateAdmin()
        }
    }

    private fun validateAccess(joinPoint: ProceedingJoinPoint, validator: () -> Unit = {}): Any? {
        if (hasUnAuthAnnotation(joinPoint) || hasApiHeader()) {
            return joinPoint.proceed()
        }

        throwIf(currentPrincipal() == null, errorCode = ErrorCode.USER_NOT_LOGIN_ERROR)
        validator()
        return joinPoint.proceed()
    }

    private fun validateRole(requireRole: RequireRole) {
        if (isAdmin()) return
        val userRoles = currentPrincipal()?.roles.orEmpty()
        val requiredRoles = requireRole.value.toList()

        val passed = when (requireRole.mode) {
            RequireRole.RoleCheckMode.ANY -> requiredRoles.any { it in userRoles }
            RequireRole.RoleCheckMode.ALL -> requiredRoles.all { it in userRoles }
        }

        if (!passed) {
            throw ErrorCode.AUTH_ERROR.exception("权限不足，需要角色：${requiredRoles.joinToString(", ")}")
        }
    }

    private fun validatePermissionCodes(requirePermission: RequirePermission) {
        if (isAdmin()) return
        val userPermissions = currentPrincipal()?.permissions.orEmpty()
        val required = requirePermission.value.toList()

        val passed = when (requirePermission.mode) {
            RequirePermission.PermissionCheckMode.ANY -> required.any { it in userPermissions }
            RequirePermission.PermissionCheckMode.ALL -> required.all { it in userPermissions }
        }

        if (!passed) {
            throw ErrorCode.AUTH_ERROR.exception("权限不足，需要权限：${required.joinToString(", ")}")
        }
    }

    private fun validateAdmin() {
        throwIf(!isAdmin(), "需要管理员权限", errorCode = ErrorCode.AUTH_ERROR)
    }

    private fun hasUnAuthAnnotation(joinPoint: ProceedingJoinPoint): Boolean {
        val method = (joinPoint.signature as MethodSignature).method
        return method.isAnnotationPresent(UnAuth::class.java)
    }

    private fun hasApiHeader(): Boolean {
        val request = currentRequest()
        val apiKey = request?.getHeader(properties.apiHeader)
        return !properties.apiKey.isNullOrBlank() && apiKey == properties.apiKey
    }
}

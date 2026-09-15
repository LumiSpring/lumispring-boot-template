package com.lumispring.framework.security.config.annotation

/**
 * 权限编码校验。
 *
 * 管理员角色默认绕过。未登录仍会失败。
 *
 * @param value 需要的权限编码，如 `user:create`
 * @param mode ANY 满足其一，ALL 必须全部具备
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
    vararg val value: String,
    val mode: PermissionCheckMode = PermissionCheckMode.ANY
) {
    enum class PermissionCheckMode {
        ANY,
        ALL
    }
}

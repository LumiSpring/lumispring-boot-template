package com.lumispring.framework.security.config.annotation

/**
 * 角色校验注解
 * 用于标注需要特定角色才能访问的方法
 *
 * @param value 需要的角色编码列表，满足其一即可
 * @param mode 校验模式：ANY-满足任意一个角色即可，ALL-必须满足所有角色
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireRole(
    vararg val value: String,
    val mode: RoleCheckMode = RoleCheckMode.ANY
) {
    enum class RoleCheckMode {
        ANY,  // 满足任意一个角色即可
        ALL   // 必须满足所有角色
    }
}

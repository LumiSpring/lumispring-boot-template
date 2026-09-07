package com.lumispring.framework.security.config.annotation

/**
 * 管理员权限校验注解
 * 用于标注需要管理员角色才能访问的方法
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireAdmin

package com.lumispring.framework.security.config.annotation

/**
 * 登录校验注解
 * 用于标注需要登录才能访问的方法
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireLogin

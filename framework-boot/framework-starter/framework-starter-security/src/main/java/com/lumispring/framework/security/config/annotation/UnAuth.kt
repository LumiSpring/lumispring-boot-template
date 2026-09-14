package com.lumispring.framework.security.config.annotation

/**
 * 匿名访问注解
 * 用于标注不需要登录即可访问的方法
 * 优先级高于 @RequireLogin、@RequireRole、@RequireAdmin
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UnAuth

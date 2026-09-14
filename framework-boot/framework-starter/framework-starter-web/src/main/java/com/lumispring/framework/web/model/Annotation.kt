package com.lumispring.framework.web.model

/**
 * 当类或者方法上有此注解时，不会对返回结果进行封装
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreResponse
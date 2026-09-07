package com.lumispring.framework.base.extension

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy(false)
class ApplicationContextHolder:ApplicationContextAware {

    companion object {
        lateinit var context: ApplicationContext
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}

/**
 * 获取当前ApplicationContext
 */
fun getApplicationContext() = ApplicationContextHolder.context

/**
 * 获取bean
 * @param name bean名称
 * @return bean
 * @throws Exception bean不存在时抛出异常
 */
fun getBean(name: String): Any {
    return ApplicationContextHolder.context.getBean(name)
}

/**
 * 获取bean
 * @param name bean名称
 * @return bean / null
 * bean不存在时返回null
 */
fun getBeanOrNull(name:String): Any? {
    return try {
        getBean(name)
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取bean
 * @param clazz bean类型
 * @return bean
 * @throws Exception bean不存在时抛出异常
 */
fun <T> getBean(clazz: Class<T>): T {
    return ApplicationContextHolder.context.getBean(clazz)
}

/**
 * 获取bean
 * @param clazz bean类型
 * @return bean / null
 * bean不存在时返回null
 */
fun <T> getBeanOrNull(clazz: Class<T>): T? {
    return try {
        getBean(clazz)
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取bean
 * @param name bean名称
 * @param clazz bean类型
 * @return bean
 * @throws Exception bean不存在时抛出异常
 */
fun <T> getBean(name: String, clazz: Class<T>): T {
    return ApplicationContextHolder.context.getBean(name, clazz)
}

/**
 * 获取bean
 * @param name bean名称
 * @param clazz bean类型
 * @return bean / null
 * bean不存在时返回null
 */
fun <T> getBeanOrNull(name: String, clazz: Class<T>): T? {
    return try {
        getBean(name, clazz)
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取bean
 * @param clazz bean类型
 * @return bean
 * @throws Exception bean不存在时抛出异常
 */
fun <T> getBeans(clazz: Class<T>): Map<String, T> {
    return ApplicationContextHolder.context.getBeansOfType(clazz)
}

/**
 * 获取bean
 * @param clazz bean类型
 * @return bean / null
 * bean不存在时返回null
 */
fun <T> getBeansOrNull(clazz: Class<T>): Map<String, T>? {
    return try {
        getBeans(clazz)
    } catch (e: Exception) {
        null
    }
}
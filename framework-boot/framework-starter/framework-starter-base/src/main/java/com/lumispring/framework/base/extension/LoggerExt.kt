package com.lumispring.framework.base.extension

import com.lumispring.framework.base.designpattern.DefaultCache
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Logger缓存
 */
private val loggerCache = com.lumispring.framework.base.designpattern.DefaultCache<String, Logger>()

/**
 * 使用方式：class xxx { val logger = logger() } 或者 val logger = XXX.logger()
 */
@JvmName("logger1")
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun logger(loggerName: String): Logger {
    return LoggerFactory.getLogger(loggerName)
}

fun logger():Logger{
    return LoggerFactory.getLogger(findCaller().className)
}

private fun findCaller(): StackTraceElement {
    // 获取堆栈信息
    val stackTrace = Thread.currentThread().stackTrace

    // 找到最后一个日志类标识的下标
    val i = stackTrace.indexOfLast { "LoggerKt" in it.className }

    return stackTrace[i + 1]
}

fun <T> logger(clz: Class<T>): Logger {
    return LoggerFactory.getLogger(clz)
}

fun <T : Any> logger(clz: KClass<T>): Logger {
    return LoggerFactory.getLogger(clz.java)
}

inline fun <reified T> T.logInfo(message: String) {
    logger().info(message)
}

inline fun <reified T> T.logDebug(message: String) {
    logger().debug(message)
}

inline fun <reified T> T.logError(message: String) {
    logger().error(message)
}

inline fun <reified T> T.logWarn(message: String) {
    logger().warn(message)
}

fun logInfo(message: String) {
    logger().info(message)
}

fun logDebug(message: String) {
    logger().debug(message)
}

fun logError(message: String) {
    logger().error(message)
}

fun logWarn(message: String) {
    logger().warn(message)
}

/**
 * 获取缓存中的logger
 * @param className 全限定类名 eg:com.xx.xx.X
 */
fun getLoggerByCache(className:String?): Logger?{
    if (className.isNull()) return null
    return loggerCache.getOrPut(className){
        logger(Class.forName(className))
    }
}
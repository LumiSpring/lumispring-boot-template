package com.lumispring.framework.web.handler

import com.lumispring.framework.base.designpattern.ChainerContext
import com.lumispring.framework.base.designpattern.IChain
import com.lumispring.framework.base.extension.logger
import com.lumispring.framework.base.extension.toJsonString
import com.lumispring.framework.web.extension.currentRequest
import com.lumispring.framework.web.extension.currentTraceId
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import java.io.File

private const val CONTROLLER_MARK = "IControllerAop"

data class AopInvocation(
    var startTimestamp: Long,
    var requestMethod: String,
    var requestURI: String,
    var requestParams: List<Any?>,
    var returnValue: Any?,
    var throwable: Throwable?
)

/**
 * 抽象控制器切面，继承该切面，实现before和afterReturning方法即可
 */
abstract class IControllerAop : IChain<Any>() {

    override fun handler(requestParam: Any) {
        if (requestParam is ProceedingJoinPoint) before(requestParam)
        if (requestParam is AopInvocation) afterReturning(requestParam)
    }

    override fun mark(): String {
        return CONTROLLER_MARK
    }

    abstract fun before(joinPoint: ProceedingJoinPoint)

    abstract fun afterReturning(invocation: AopInvocation)
}

/**
 * 控制器日志切面
 */
class LogControllerAop : IControllerAop() {
    private var logger = logger()

    override fun before(joinPoint: ProceedingJoinPoint) {}

    override fun afterReturning(invocation: AopInvocation) {
        // 非api调用行为不记录
        if (!invocation.requestURI.contains("/api")) return
        val requestParam = invocation.requestParams.filterNotNull().joinToString(",") {
            when (it) {
                is ByteArray -> "[ByteArray]"
                is File -> "File(${it.name})"
                else -> try{
                    it.toJsonString()
                }catch (e:Exception){ "" }
            }
        }
        logger.info("""
           traceID: ${currentTraceId()} 
           ${invocation.requestMethod} ${invocation.requestURI} $requestParam executeTime: ${System.currentTimeMillis() - invocation.startTimestamp}ms
        """.trimIndent())
    }
    override fun getOrder(): Int {
        return 1
    }
}


/**
 * 控制器方法执行切面
 */
@Aspect
class ControllerAspect {
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    fun controllerPointcut(){}

    @Around("controllerPointcut()")
    fun handlerControllerAround(joinPoint: ProceedingJoinPoint): Any {
        ChainerContext.handle<Any>(CONTROLLER_MARK, joinPoint)
        val startTime = System.currentTimeMillis()
        var result: Any? = null
        try {
            result = joinPoint.proceed()
        } catch (e: Throwable){
            handlerInvocation(startTime, joinPoint, null, e)
            throw e
        }
        handlerInvocation(startTime, joinPoint, result, null)

        return result
    }

    private fun handlerInvocation(startTimestamp: Long, joinPoint: ProceedingJoinPoint, returnValue: Any?, throwable: Throwable?){
        val httpServletRequest = currentRequest()
        val invocation = AopInvocation(
            startTimestamp = startTimestamp,
            requestParams = joinPoint.args.toList(),
            requestMethod = httpServletRequest?.method ?: "",
            requestURI = httpServletRequest?.requestURI ?: "",
            returnValue = returnValue,
            throwable = throwable
        )
        ChainerContext.handle<Any>(CONTROLLER_MARK, invocation, true)
    }
}
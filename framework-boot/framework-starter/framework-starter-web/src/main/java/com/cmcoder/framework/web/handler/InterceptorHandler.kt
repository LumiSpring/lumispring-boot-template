package com.lumispring.framework.web.handler

import com.lumispring.framework.base.extension.randomUuid
import com.lumispring.framework.web.extension.RequestContext
import com.lumispring.framework.web.extension.TraceIdContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception

/**
 * 数据初始化拦截器
 */
class DataInitInterceptor: HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        RequestContext.setRequest(request)
        RequestContext.setResponse(response)
        val traceId = request.let {
            it.getHeader("traceId") ?: it.getHeader("trace-id") ?: it.getHeader("Trace-Id") ?: it.getHeader("TraceId")
        } ?: randomUuid()
        TraceIdContext.setTraceId(traceId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        RequestContext.removeRequest()
        RequestContext.removeResponse()
        TraceIdContext.removeTraceId()
    }
}
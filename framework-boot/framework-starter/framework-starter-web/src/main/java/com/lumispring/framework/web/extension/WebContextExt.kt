package com.lumispring.framework.web.extension

import com.alibaba.ttl.TransmittableThreadLocal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.context.ContextLoader

fun getWebContext() = ContextLoader.getCurrentWebApplicationContext()

/**
 * 获取当前application作用域
 */
fun getServletContext() = getWebContext()?.servletContext

@Component
class RequestContext {
    companion object {
        private val REQUEST_THREAD_LOCAL = TransmittableThreadLocal<HttpServletRequest?>()
        private val RESPONSE_THREAD_LOCAL = TransmittableThreadLocal<HttpServletResponse?>()

        fun setRequest(request: HttpServletRequest){
            REQUEST_THREAD_LOCAL.set(request)
        }

        fun getRequest():HttpServletRequest?{
           return REQUEST_THREAD_LOCAL.get()
        }

        fun setResponse(response: HttpServletResponse){
            RESPONSE_THREAD_LOCAL.set(response)
        }

        fun getResponse():HttpServletResponse?{
            return RESPONSE_THREAD_LOCAL.get()
        }

        fun removeRequest(){
            REQUEST_THREAD_LOCAL.remove()
        }

        fun removeResponse(){
            RESPONSE_THREAD_LOCAL.remove()
        }
    }
}

/**
 * 获取当前request作用域
 */
fun currentRequest() : HttpServletRequest? {
    return RequestContext.getRequest()
}

/**
 * 获取当前response作用域
 */
fun currentResponse() :HttpServletResponse? {
    return RequestContext.getResponse()
}

@Component
class TraceIdContext {
    companion object {
        private val TRACE_ID_THREAD_LOCAL = TransmittableThreadLocal<String?>()
        /**
         * 设置traceId
         * @param traceId
         */
        fun setTraceId(traceId: String) {
            TRACE_ID_THREAD_LOCAL.set(traceId)
        }

        /**
         * 获取traceId
         */
        fun getTraceId():String? {
            return TRACE_ID_THREAD_LOCAL.get()
        }

        /**
         * 移除traceId
         */
        fun removeTraceId(){
            TRACE_ID_THREAD_LOCAL.remove()
        }
    }
}

/**
 * 获取当前线程的traceId
 */
fun currentTraceId():String? {
    return try {
        TraceIdContext.getTraceId()
    } catch (e: Exception){ null }
}

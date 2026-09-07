package com.lumispring.framework.web.handler

import com.baomidou.mybatisplus.core.metadata.IPage
import com.fasterxml.jackson.databind.ObjectMapper
import com.lumispring.framework.base.extension.logger
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.database.mysql.extension.toResponse
import com.lumispring.framework.web.extension.currentTraceId
import com.lumispring.framework.web.model.IgnoreResponse
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux


/**
 * 统一返回响应处理
 */
@ControllerAdvice(annotations = [RestController::class])
@Hidden
class ResponseAdviceHandler : ResponseBodyAdvice<Any> {

    private val logger = logger()

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
        /**
         * 不需要拦截的类
         */
        val EXCLUDE = listOf(
            "Swagger2Controller",
            "Swagger2ControllerWebMvc",
            "ApiResourceController",
            "SwaggerConfigResource",
            "OpenApiWebMvcResource"
        )

        /**
         * 需要封装的ContentType类型
         */
        val HANDLER_CONTENT_TYPES = listOf(
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_JSON_UTF8_VALUE,
            MediaType.TEXT_PLAIN_VALUE,
            MediaType.TEXT_HTML_VALUE
        )
    }

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        // 如果方法所在的类在EXCLUDE中，则不拦截
        if (returnType.method?.declaringClass?.simpleName in EXCLUDE) {
            return false
        }

        // 新增：排除 SpringDoc 和 Knife4j 相关包
        val packageName = returnType.method?.declaringClass?.packageName
        if (packageName?.startsWith("org.springdoc") == true ||
            packageName?.startsWith("com.github.xiaoymin") == true) {
            return false
        }

        // 如果方法或类上有IgnoreResponse注解，则不拦截
        if ((returnType.method?.isAnnotationPresent(IgnoreResponse::class.java) == true) || returnType.declaringClass.isAnnotationPresent(
                IgnoreResponse::class.java
            )
        ) {
            return false
        }

        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // 如果body是Response或者ModelAndView或者ResponseEntity或者SseEmitter，则不封装
        if (body is Response<*> || body is ModelAndView || body is ResponseEntity<*> || body is SseEmitter || body is Flux<*>) {
            return body
        }

        // 如果ContentType不在HANDLER_TYPES中，则不封装
        if (selectedContentType.toString() !in HANDLER_CONTENT_TYPES) {
            return body
        }

        // 统一返回类型是application/json
        response.headers.contentType = MediaType.APPLICATION_JSON

        if (body is IPage<*>) {
            return body.toResponse()
        }

        val res = Response.success(body).apply {
            this.traceId = currentTraceId()
        }
        // 如果是String类型，则需要转为json字符串
        if (body is String) {
            return objectMapper.writeValueAsString(res)
        }

        return res
    }
}
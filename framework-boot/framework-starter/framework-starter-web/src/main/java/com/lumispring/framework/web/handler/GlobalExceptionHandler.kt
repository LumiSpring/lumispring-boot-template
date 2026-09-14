package com.lumispring.framework.web.handler

import com.lumispring.framework.base.extension.logger
import com.lumispring.framework.base.model.AbstractException
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.web.extension.currentTraceId
import com.lumispring.framework.web.extension.resRedirect
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException


@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {

    private val logger = logger()

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(request: HttpServletRequest, e: NoResourceFoundException): ResponseEntity<Response<*>>{
        if (request.requestURI.contains(".html")){
            resRedirect("/error/404.html")
        } else if (request.requestURI.contains("/api")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(ErrorCode.API_NOT_FIND).apply { this.traceId = currentTraceId() })
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error(ErrorCode.RESOURCE_NOT_FIND).apply { this.traceId = currentTraceId() })
    }

    @ExceptionHandler(AbstractException::class)
    fun handleAbstractException(request: HttpServletRequest, e: AbstractException): ResponseEntity<Response<*>>{
        recordLog(request, e)
        if (!request.requestURI.contains("/api")) resRedirect("/error/500.html")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.error(e.code, e.msg).apply { this.traceId = currentTraceId() })
    }

    @ExceptionHandler(Exception::class)
    fun handleDefaultException(request: HttpServletRequest, e: Exception): ResponseEntity<Response<*>>{
        recordLog(request, e)
        if (!request.requestURI.contains("/api")) resRedirect("/error/500.html")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.error().apply { this.traceId = currentTraceId() })
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(request: HttpServletRequest, e: MethodArgumentNotValidException): ResponseEntity<Response<*>>{
        val exceptionStr = e.bindingResult.fieldError?.defaultMessage ?: ""
        logger.error("请求参数校验异常：$exceptionStr")
        recordLog(request, e)
        if (!request.requestURI.contains("/api")) resRedirect("/error/500.html")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error(ErrorCode.ARGUMENT_NOT_VALID_ERROR, exceptionStr).apply { this.traceId = currentTraceId() })
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(request: HttpServletRequest, e: ValidationException): ResponseEntity<Response<*>>{
        val exceptionStr = e.message?.split(":")?.last()
        logger.error("服务参数校验异常：$exceptionStr")
        recordLog(request, e)
        if (!request.requestURI.contains("/api")) resRedirect("/error/500.html")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.error(ErrorCode.ARGUMENT_NOT_VALID_ERROR, exceptionStr).apply { this.traceId = currentTraceId() })
    }


    /**
     * 记录日志
     */
    fun recordLog(request: HttpServletRequest, e: Exception){
        logger.error("""
            traceID: ${currentTraceId()} 
            ${request.method} ${if (request.queryString.isNullOrEmpty()) request.requestURI else "${request.requestURI}?${request.queryString}"} :$e
        """.trimIndent())
        e.printStackTrace()
    }

}
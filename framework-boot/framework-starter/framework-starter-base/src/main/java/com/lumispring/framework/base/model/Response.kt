package com.lumispring.framework.base.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.lumispring.framework.base.extension.ifElse
import com.lumispring.framework.base.extension.isNotNullOrEmpty

/**
 * 统一响应结果
 * @param code 状态码
 * @param message 状态信息
 * @param data 响应数据
 * @param pageNum 当前页码
 * @param pageSize 每页数量
 * @param total 总行数
 * @param pages 总页数
 * @param traceId 链路追踪ID
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response<T>(
    var code: String? = null,
    var message: String? = null,
    var data: T? = null,
    var pageNum: Long? = null,
    var pageSize: Long? = null,
    var total: Long? = null,
    var pages: Long? = null,
    var traceId: String? = null
) {
    @JsonProperty("success")
    fun isSuccess(): Boolean {
        return code == SUCCESS.code
    }

    /**
     * 静态方法
     */
    companion object {
        private var SUCCESS = SuccessCode.SUCCESS

        fun <T> success(data: T): Response<T> {
            return Response<T>().apply {
                this.code = SUCCESS.code
                this.message = SUCCESS.message
                this.data = data
            }
        }

        fun <T> success(data: T, pageNum: Long? = null, pageSize: Long? = null, total: Long? = null, pages: Long? = null): Response<T> {
            return Response<T>().apply {
                this.code = SUCCESS.code
                this.message = SUCCESS.message
                this.data = data
                this.pageNum = pageNum
                this.pageSize = pageSize
                this.total = total
                this.pages = pages
            }
        }

        fun error(message: String): Response<Void> {
            return Response<Void>().apply {
                this.code = ErrorCode.SERVICE_ERROR.code
                this.message = message
            }
        }

        fun error(code: String, message: String): Response<Void> {
            return Response<Void>().apply {
                this.code = code
                this.message = message
            }
        }

        fun error(errorCode: ErrorCode = ErrorCode.SERVICE_ERROR, message: String? = null): Response<Void> {
            return Response<Void>().apply {
                this.code = errorCode.code
                this.message = message ?: errorCode.message
            }
        }
    }
}

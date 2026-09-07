package com.lumispring.framework.base.model

import com.lumispring.framework.base.extension.getLoggerByCache
import com.lumispring.framework.base.extension.logger

/**
 * 基本返回码
 */
interface BaseCode {
    fun code(): String
    fun message(): String
}

/**
 * 成功返回码
 */
enum class SuccessCode(
    var code: String,
    var message: String
) : BaseCode {
    SUCCESS("200", "success");

    override fun code(): String {
        return code
    }

    override fun message(): String {
        return message
    }
}

/**
 * 错误返回码
 */
enum class ErrorCode(
    var code: String,
    var message: String,
    var exClazz: Class<out AbstractException>
) : BaseCode {
    // ========== 一级宏观错误码 客户端错误 ==========
    AUTH_ERROR("A000001", "权限异常", AuthException::class.java),
    ARGUMENT_NOT_VALID_ERROR("A000002", "客户端参数校验不通过", AuthException::class.java),
    RESOURCE_NOT_FIND("A000003", "请求资源不存在", AuthException::class.java),
    API_NOT_FIND("A000004", "api不存在", AuthException::class.java),

    // ========== 二级宏观错误码 用户注册错误 ==========
    USER_REGISTER_ERROR("A000100", "用户注册错误", AuthException::class.java),
    USER_NAME_VERIFY_ERROR("A000110", "用户名校验失败", AuthException::class.java),
    USER_NAME_EXIST_ERROR("A000111", "用户名已存在", AuthException::class.java),
    USER_NAME_SENSITIVE_ERROR("A000112", "用户名包含敏感词", AuthException::class.java),
    USER_NAME_SPECIAL_CHARACTER_ERROR("A000113", "用户名包含特殊字符", AuthException::class.java),
    PASSWORD_VERIFY_ERROR("A000120", "密码校验失败", AuthException::class.java),
    PASSWORD_SHORT_ERROR("A000121", "密码长度不够", AuthException::class.java),
    PHONE_VERIFY_ERROR("A000151", "手机格式校验失败", AuthException::class.java),

    // ========== 二级宏观错误码 系统请求缺少幂等Token ==========
    IDEMPOTENT_TOKEN_NULL_ERROR("A000200", "幂等Token为空", AuthException::class.java),
    IDEMPOTENT_TOKEN_DELETE_ERROR("A000201", "幂等Token已被使用或失效", AuthException::class.java),

    IDEMPOTENT_DUPLICATE_ERROR("A000202", "幂等重复请求", AuthException::class.java),

    // ========== 二级宏观错误码 用户注册错误 ==========
    USER_LOGIN_ERROR("A000300", "用户登录错误", AuthException::class.java),
    USER_LOGIN_NOT_EXIST_ERROR("A000301", "用户不存在", AuthException::class.java),
    USER_LOGIN_PASSWORD_ERROR("A000302", "用户账号或密码有误", AuthException::class.java),

    USER_NOT_LOGIN_ERROR("A000303", "用户未登录", AuthException::class.java),


    // ========== 业务通用报错 ===========
    BUSINESS_ERROR("B000001", "业务异常", BusinessException::class.java),


    // ========== 一级宏观错误码 系统执行出错 ==========
    SERVICE_ERROR("S000001", "系统执行出错", ServiceException::class.java),
    // ========== 二级宏观错误码 系统执行超时 ==========
    SERVICE_TIMEOUT_ERROR("S000100", "系统执行超时", ServiceException::class.java),
    SERVICE_ASSERT_ERROR("S000101", "系统断言失败", ServiceException::class.java),
    SERVICE_BUSY_ERROR("S000102", "系统繁忙，请稍后重试", ServiceException::class.java),

    // ========== 二级宏观错误码 定时任务出错 ==========
    SERVICE_SCHEDULED_ERROR("S000200", "定时任务失败", ServiceException::class.java),
    SERVICE_SCHEDULED_INIT_ERROR("S000201", "定时任务初始化失败", ServiceException::class.java),
    SERVICE_SCHEDULED_RUN_ERROR("S000202", "定时任务执行失败", ServiceException::class.java),

    // ========== 二级宏观错误码 参数校验出错 ==========
    SERVICE_PARAM_ERROR("S000300", "内部参数出错", ServiceException::class.java),
    SERVICE_PARAM_TYPE_ERROR("S000301", "内部参数类型错误", ServiceException::class.java),
    SERVICE_PARAM_VALIDATION_ERROR("S000302", "内部参数校验出错", ServiceException::class.java),
    SERVICE_PARAM_FORMAT_ERROR("S000303", "内部参数格式错误", ServiceException::class.java),

    // ========== 二级宏观错误码 文件出错 ==========
    SERVICE_FILE_ERROR("S000400", "文件出错", ServiceException::class.java),
    SERVICE_FILE_UPLOAD_ERROR("S000401", "文件上传失败", ServiceException::class.java),
    SERVICE_FILE_DOWNLOAD_ERROR("S000402", "文件下载失败", ServiceException::class.java),

    // ========== 二级宏观错误码 WebSocket出错 ==========
    SERVICE_WEBSOCKET_ERROR("S000500", "WebSocket出错", ServiceException::class.java),
    SERVICE_WEBSOCKET_CONNECT_ERROR("S000501", "WebSocket连接失败", ServiceException::class.java),
    SERVICE_WEBSOCKET_CLOSE_ERROR("S000502", "WebSocket关闭失败", ServiceException::class.java),
    SERVICE_WEBSOCKET_SEND_ERROR("S000503", "WebSocket发送失败", ServiceException::class.java),


    // ========== 一级宏观错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("R000001", "调用第三方服务出错", RemoteException::class.java),
    REQUEST_PARAM_ERROR("R000002", "内部请求参数错误", RemoteException::class.java),
    MQ_MESSAGE_SEND_ERROR("R000100", "MQ消息发送失败", RemoteException::class.java),
    MQ_MESSAGE_PARAM_ERROR("R000101", "MQ消息参数错误", RemoteException::class.java);

    companion object {
        private val logger = logger()
        // 打印日志时需要忽略的堆栈类型
        private val UN_LOG_CLASS = setOf(
            ErrorCode::class.java.simpleName,
            "ValidationExtKt"
        )
    }

    override fun code(): String {
        return code
    }

    override fun message(): String {
        return message
    }

    fun exception(message: String? = null, log: String? = null): AbstractException {
        printLog(log)
        return this.exClazz.getConstructor(ErrorCode::class.java, String::class.java).newInstance(this, message ?: this.message)
    }

    fun printLog(log: String? = null) {
        if (log == null) return
        // 获取当前堆栈信息
        val stackTrace = Thread.currentThread().stackTrace
        var element: StackTraceElement? = null
        for (i in 2 until stackTrace.size){
            if (UN_LOG_CLASS.all { !stackTrace[i].className.contains(it) }){
                element = stackTrace[i]
                break
            }
        }
        (getLoggerByCache(element?.className) ?: logger).error("""
            ${if (element != null) "[${element.fileName}:${element.className}.${element.methodName}] " else ""}error: $log
        """.trimIndent())
    }
}
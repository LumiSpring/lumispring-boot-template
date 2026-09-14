package com.lumispring.framework.base.model

/**
 * 抽象异常类
 */
abstract class AbstractException(
    errorCode: String? = null,
    errorMessage: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(errorMessage ?: ErrorCode.SERVICE_ERROR.message, throwable) {
    val code:String = errorCode ?: ErrorCode.SERVICE_ERROR.code
    val msg:String = errorMessage ?: ErrorCode.SERVICE_ERROR.message
    constructor(errorCode: String) : this(errorCode, null, null)
}

/**
 * 服务端异常
 */
data class ServiceException(
    val errorCode: ErrorCode? = null,
    val errorMessage: String? = null,
    val throwable: Throwable? = null,
) : AbstractException(errorCode?.code ,errorMessage, throwable) {

    constructor(errorMessage: String) : this(ErrorCode.SERVICE_ERROR, errorMessage, null)
    constructor(throwable: Throwable) : this(ErrorCode.SERVICE_ERROR, null, throwable)
    constructor(errorCode: ErrorCode, errorMessage: String) : this(errorCode,errorMessage, null)

    override fun toString(): String {
        return "ServiceException(code='$code', msg='$msg')"
    }
}

/**
 * 认证异常
 */
data class AuthException(
    val errorCode: ErrorCode? = null,
    val errorMessage: String? = null,
    val throwable: Throwable? = null,
) : AbstractException(errorCode?.code ,errorMessage, throwable) {

    constructor(errorMessage: String) : this(ErrorCode.AUTH_ERROR, errorMessage, null)
    constructor(throwable: Throwable) : this(ErrorCode.AUTH_ERROR, null, throwable)
    constructor(errorCode: ErrorCode, errorMessage: String) : this(errorCode,errorMessage, null)

    override fun toString(): String {
        return "AuthException(code='$code', msg='$msg')"
    }
}

/**
 * 业务异常
 */
data class BusinessException(
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val throwable: Throwable? = null,
) : AbstractException(errorCode ,errorMessage, throwable) {
    constructor(errorCode: String, errorMessage: String) : this(errorCode, errorMessage, null)
    constructor(errorMessage: String) : this(ErrorCode.BUSINESS_ERROR.code, errorMessage, null)
    constructor(throwable: Throwable) : this(ErrorCode.BUSINESS_ERROR.code, null, throwable)
    constructor(errorCode: ErrorCode, errorMessage: String) : this(errorCode.code, errorMessage, null)

    override fun toString(): String {
        return "BusinessException(code='$code', msg='$msg')"
    }
}

/**
 * 远程调用异常
 */
data class RemoteException(
    val errorCode: ErrorCode? = null,
    val errorMessage: String? = null,
    val throwable: Throwable? = null,
) : AbstractException(errorCode?.code ,errorMessage, throwable) {

    constructor(errorMessage: String) : this(ErrorCode.REMOTE_ERROR, errorMessage, null)
    constructor(throwable: Throwable) : this(ErrorCode.REMOTE_ERROR, null, throwable)
    constructor(errorCode: ErrorCode, errorMessage: String) : this(errorCode,errorMessage, null)

    override fun toString(): String {
        return "RemoteException(code='$code', msg='$msg')"
    }
}
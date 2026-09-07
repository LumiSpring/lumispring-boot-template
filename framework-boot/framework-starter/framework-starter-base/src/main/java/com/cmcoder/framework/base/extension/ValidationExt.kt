package com.lumispring.framework.base.extension

import com.lumispring.framework.base.model.AbstractException
import com.lumispring.framework.base.model.BusinessException
import com.lumispring.framework.base.model.ErrorCode
import kotlin.contracts.contract

fun Any?.isNull(): Boolean {
    contract {
        // 如果返回值是true，则调用者为null
        returns(true) implies (this@isNull == null)
        // 如果返回值是false，则调用者不为null
        returns(false) implies (this@isNull != null)
    }
    return this == null
}

fun Any?.isNotNull(): Boolean {
    contract {
        returns(true) implies (this@isNotNull != null)
        returns(false) implies (this@isNotNull == null)
    }
    return this != null
}

fun Any?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }
    if (this == null) return true
    return when (this) {
        is String -> this.isEmpty()
        is Map<*, *> -> this.isEmpty()
        is Array<*> -> this.isEmpty()
        is Iterable<*> -> this.count() == 0
        is Sequence<*> -> this.count() == 0
        else -> false
    }
}

fun Any?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}

fun <T : Any> ifNull(origin: T?, default: T): T {
    return if (origin.isNotNull()) origin else default
}

@JvmName("ifNull1")     // 为了避免java重载时和上面的ifNull方法冲突，所以使用JvmName注解来定义不同名称
fun <T : Any> T?.ifNull(default: T): T {
    return if (this.isNotNull()) this else default
}

/**
 * 如果为true则返回trueValue，否则返回falseValue
 * @param   condition   条件
 * @param   trueValue   为true时的值
 * @param   falseValue  为false时的值
 */
fun <T : Any> ifElse(condition: Boolean, trueValue: T?, falseValue: T?): T? {
    return if (condition) trueValue else falseValue
}

/**
 * 如果为true则返回trueValue，否则返回falseValue
 * @param   trueValue   为true时的值
 * @param   falseValue  为false时的值
 */
@JvmName("ifElse1")
fun <T : Any> Boolean.ifElse(trueValue: T?, falseValue: T?): T? {
    return if (this) trueValue else falseValue
}

fun Boolean?.isTrue(): Boolean {
    return if (this.isNotNull()) this else false
}

fun Boolean?.isFalse(): Boolean {
    return if (this.isNotNull()) !this else false
}

fun <T : Any> ifTrue(condition: Boolean, block: () -> T?): T? {
    if (condition.isTrue()) return block()
    return null
}

fun <T : Any> ifFalse(condition: Boolean, block: () -> T?): T? {
    if (condition.isFalse()) return block()
    return null
}

@JvmName("ifTrue1")
fun <T : Any> Boolean?.ifTrue(block: () -> T?): T? {
    if (this.isTrue()) return block()
    return null
}

@JvmName("ifFalse1")
fun <T : Any> Boolean?.ifFalse(block: () -> T?): T? {
    if (this.isFalse()) return block()
    return null
}

/**
 * 校验参数不为Null
 * @param errorMsg  错误信息
 * @param errorCode 错误码
 * @param log       日志
 */
@JvmOverloads
fun <T : Any> T?.checkNotNull(
    errorMsg: String? = null,
    errorCode: ErrorCode = ErrorCode.ARGUMENT_NOT_VALID_ERROR,
    log: String? = null,
): T {
    contract {
        returns() implies (this@checkNotNull != null)
    }
    if (this.isNull()) throw errorCode.exception(message = errorMsg, log = log)
    return this
}

/**
 * 校验参数不为空
 * @param errorMsg  错误信息
 * @param errorCode 错误码
 * @param log       日志
 */
@JvmOverloads
fun <T : Any> T.checkNotEmpty(
    errorMsg: String? = null,
    errorCode: ErrorCode = ErrorCode.ARGUMENT_NOT_VALID_ERROR,
    log: String? = null,
): T {
    if (this.isNullOrEmpty()) throw errorCode.exception(message = errorMsg, log = log)
    return this
}

/**
 * 校验参数不为Null或空
 * @param errorMsg  错误信息
 * @param errorCode 错误码
 * @param log       日志
 */
@JvmOverloads
fun <T : Any> T?.checkNotNullOrEmpty(
    errorMsg: String? = null,
    errorCode: ErrorCode = ErrorCode.ARGUMENT_NOT_VALID_ERROR,
    log: String? = null,
): T {
    contract {
        returns() implies (this@checkNotNullOrEmpty != null)
    }
    if (this.isNullOrEmpty()) throw errorCode.exception(message = errorMsg, log = log)
    return this
}

/**
 * 校验参数为true
 * @param errorMsg  错误信息
 * @param errorCode 错误码
 * @param log       日志
 * @param block  校验逻辑
 */
@JvmOverloads
fun <T : Any> T?.check(
    errorMsg: String? = null,
    errorCode: ErrorCode = ErrorCode.ARGUMENT_NOT_VALID_ERROR,
    log: String? = null,
    block: (T?) -> Boolean
): T? {
    contract {
        returns() implies (this@check != null)
    }
    if (!block(this)) throw errorCode.exception(message = errorMsg, log = log)
    return this
}

/**
 * 校验参数为true
 * @param errorMsg  错误信息
 * @param errorCode 错误码
 * @param log       日志
 */
@JvmOverloads
fun Boolean?.checkTrue(
    errorMsg: String? = null,
    errorCode: ErrorCode = ErrorCode.ARGUMENT_NOT_VALID_ERROR,
    log: String? = null,
): Boolean {
    contract {
        returns() implies (this@checkTrue != null)
    }
    if (this != true) throw errorCode.exception(message = errorMsg, log = log)
    return this
}

/**
 * 如果条件为true则抛出异常
 * @param condition   条件
 * @param message     错误信息
 * @param errorCode   标准错误码
 * @param code        自定义的错误码
 */
@JvmOverloads
fun throwIf(
    condition: Boolean,
    message: String? = null,
    errorCode: ErrorCode? = null,
    code: String? = null,
) {
    if (!condition) return
    if (errorCode != null) throw errorCode.exception(message)
    throw BusinessException(message ?: ErrorCode.BUSINESS_ERROR.message, code ?: ErrorCode.BUSINESS_ERROR.code)
}

/**
 * 如果参数为空则抛出异常
 * @param message     错误信息
 * @param errorCode   错误码
 * @param code        自定义的错误码
 */
@JvmOverloads
fun <T> T?.throwIfEmpty(
    message: String? = null,
    errorCode: ErrorCode? = null,
    code: String? = null,
): T {
    contract {
        returns() implies (this@throwIfEmpty != null)
    }
    throwIf(this.isNullOrEmpty(), message, errorCode, code)
    return this!!
}

/**
 * 如果参数为空白则抛出异常
 * @param message     错误信息
 * @param errorCode   错误码
 * @param code        自定义的错误码
 */
@JvmOverloads
fun <T> T?.throwIfNull(
    message: String? = null,
    errorCode: ErrorCode? = null,
    code: String? = null,
): T {
    contract {
        returns() implies (this@throwIfNull != null)
    }
    throwIf(this == null, message, errorCode, code)
    return this!!
}
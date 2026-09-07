package com.lumispring.framework.base.extension

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import kotlin.math.max
import kotlin.math.min

/**
 * 用来参数脱敏
 * @param start 开始位置
 * @param end 结束位置
 * @param replace 替换字符串
 */
@JvmOverloads
fun String.mask(start: Int, end: Int, replace: String = "*"): String {
    if (this.isEmpty()) return this
    val realStar = max(0, start)
    val realEnd = min(max(0, end), this.length)
    if (realStar > realEnd) return this

    // 将realStar -> realEnd 中的所有字符替换成指定的字符
    val prefix = this.substring(0, realStar)
    val suffix = this.substring(realEnd)
    return prefix + replace.repeat(realEnd - realStar) + suffix

}

/**
 * 手机号脱敏
 */
fun String.maskPhone() : String {
    return this.mask(3, 7)
}

/**
 * 身份证脱敏
 */
fun String.maskIdCard() : String {
    return this.mask(6, 14)
}

/**
 * 邮箱脱敏
 */
fun String.maskEmail() : String {
    return this.mask(2, this.indexOf("@"))
}

/**
 * 姓名脱敏
 */
fun String.maskName() : String {
    if (this.length <= 2) return this.mask(1, this.length)
    return this.mask(1, this.length - 1)
}

/**
 * 地址脱敏
 */
fun String.maskAddress() : String {
    return this.mask(this.length / 2, this.length)
}



enum class DesensitizeType(
    val type: String
) {
    PHONE("phone"),
    ID_CARD("idCard"),
    EMAIL("email"),
    NAME("name"),
    ADDRESS("address")
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer::class)
annotation class Desensitize(
    val value: DesensitizeType
)

/**
 * 脱敏序列化器
 */
class DesensitizeSerializer(
    private val annotation: Desensitize? = null
): JsonSerializer<String>(), ContextualSerializer {
    override fun serialize(
        p0: String?,
        p1: JsonGenerator?,
        p2: SerializerProvider?
    ) {
        val str = when (annotation?.value) {
            DesensitizeType.PHONE -> p0?.maskPhone()
            DesensitizeType.ID_CARD -> p0?.maskIdCard()
            DesensitizeType.EMAIL -> p0?.maskEmail()
            DesensitizeType.NAME -> p0?.maskName()
            DesensitizeType.ADDRESS -> p0?.maskAddress()
            else -> p0
        }
        p1?.writeString(str)
    }

    override fun createContextual(
        prov: SerializerProvider?,
        property: BeanProperty?
    ): JsonSerializer<*>? {
        val ann = property
            ?.getAnnotation(Desensitize::class.java)
            ?: property?.getContextAnnotation(Desensitize::class.java)

        return DesensitizeSerializer(ann)
    }

}


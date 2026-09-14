package com.lumispring.framework.base.extension

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.ser.std.StdSerializer
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
): StdSerializer<String>(String::class.java) {
    override fun serialize(
        value: String?,
        gen: JsonGenerator,
        ctxt: SerializationContext
    ) {
        val str = when (annotation?.value) {
            DesensitizeType.PHONE -> value?.maskPhone()
            DesensitizeType.ID_CARD -> value?.maskIdCard()
            DesensitizeType.EMAIL -> value?.maskEmail()
            DesensitizeType.NAME -> value?.maskName()
            DesensitizeType.ADDRESS -> value?.maskAddress()
            else -> value
        }
        gen.writeString(str)
    }

    override fun createContextual(
        ctxt: SerializationContext,
        property: BeanProperty?
    ): ValueSerializer<*> {
        val ann = property
            ?.getAnnotation(Desensitize::class.java)
            ?: property?.getContextAnnotation(Desensitize::class.java)

        return DesensitizeSerializer(ann)
    }

}

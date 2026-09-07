package com.lumispring.framework.security.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * 手机号脱敏序列化器
 * 将手机号格式化为：前 3 位 + **** + 后 4 位
 * 例如：138******88
 */
class PhoneMaskSerializer : JsonSerializer<String?>() {
    override fun serialize(value: String?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value.isNullOrBlank()) {
            gen.writeString("")
            return
        }

        // 移除所有非数字字符
        val cleanPhone = value.replace("\\D".toRegex(), "")
        
        // 根据手机号长度进行脱敏处理
        val maskedPhone = when {
            // 中国手机号 11 位
            cleanPhone.length == 11 -> {
                cleanPhone.substring(0, 3) + "******" + cleanPhone.substring(cleanPhone.length - 2)
            }
            // 其他长度，保留前 3 位和后 2 位
            cleanPhone.length > 5 -> {
                cleanPhone.substring(0, 3) + "******" + cleanPhone.substring(cleanPhone.length - 2)
            }
            // 太短的号码不脱敏
            else -> {
                cleanPhone
            }
        }

        gen.writeString(maskedPhone)
    }
}

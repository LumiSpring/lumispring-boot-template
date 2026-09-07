package com.lumispring.framework.base.extension

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone


val DEFAULT_OBJECT_MAPPER = getInitObjectMapper()

/**
 * 获取初始化后的ObjectMapper
 */
fun getInitObjectMapper(defaultObjectMapper: ObjectMapper? = null): ObjectMapper {
    val om = defaultObjectMapper ?: ObjectMapper()

    om.registerModules(
        // 设置时间格式
        getJavaTimeModule(),
        // 这样会全局添加序列化器，暂时不需要，只需要注解中用@JsonSerialize(using=xxx::class)即可
//        SimpleModule().apply {
//            // 添加脱敏序列化器
//            this.addSerializer(DesensitizeSerializer())
//        }
    )
    om.setDateFormat(SimpleDateFormat(DEFAULT_DATETIME_PATTERN))
    om.setTimeZone(TimeZone.getTimeZone(DEFAULT_ZONE_ID))
    // 默认情况下，设置null值不序列化
    om.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    // 使用 toString() 而非 name() 序列化枚举
    om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
    /**
     * 默认情况下，如果JSON中包含了Java对象中不存在的属性，ObjectMapper会抛出JsonMappingException异常。
     * 通过将FAIL_ON_UNKNOWN_PROPERTIES设置为false，ObjectMapper在遇到未知属性时不要抛出异常，而是忽略这些未知属性，继续反序列化操作。
     */
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return om
}

private fun getJavaTimeModule(): Module{
    return JavaTimeModule().apply {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)
        val dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)
        val timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)
        addSerializer(LocalDateTimeSerializer(dateTimeFormatter))
        addSerializer(LocalDateSerializer(dateFormatter))
        addSerializer(LocalTimeSerializer(timeFormatter))
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
        addDeserializer(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
    }
}


/**
 * 转成json字符串
 * @param om    ObjectMapper
 */
@JvmOverloads
fun Any.toJsonString(om: ObjectMapper = DEFAULT_OBJECT_MAPPER): String {
    return om.writeValueAsString(this)
}

/**
 * 解析json字符串
 * @param unescapeCount 反转义次数
 * @param om    ObjectMapper
 */
@JvmOverloads
inline fun <reified T> String.parseJson(unescapeCount: Int = 0, om: ObjectMapper = DEFAULT_OBJECT_MAPPER): T? {
    if (this.isNullOrEmpty()) return null

    val type = T::class.java

    // 转化后的类型也是String类型
    if (String::class.java.isAssignableFrom(type)) {
        // 如果是字符串类型，且是以双引号开头，则可通过om.readValue转化，最终去除双引号
        return if (this.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
            om.readValue(this, type)
        } else this as T
    }

    if (this.isBlank()) return null

    // 对于复杂的类型，由于 Java 的类型擦除，泛型类型信息在运行时会丢失，因此需要通过 TypeReference 来显式保留这些信息
    return try{
        // 如果是被双引号包裹的字符串，则需要反转义后并且去除多余的双引号
        if (this.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
            om.readValue(this.unescapeJson(unescapeCount).trim('"'), type)
        } else om.readValue<T>(this.unescapeJson(unescapeCount))
    } catch (e: Exception){
        if (T::class.java.simpleName == "Object"){
            // 如果是字符串类型，且是以双引号开头，则可通过om.readValue转化，最终去除双引号
            return if (this.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
                om.readValue(this, String::class.java) as T
            } else this as T
        }
        throw e
    }
}

/**
 * 解析json字符串
 * @param json json字符串
 * @param type 目标类型
 * @param unescapeCount 反转义次数
 * @param om    ObjectMapper
 */
@JvmOverloads
fun <T> parseJson(json: String, type: Class<T>, om: ObjectMapper = DEFAULT_OBJECT_MAPPER, unescapeCount: Int = 0): T?{
    if (json.isNullOrEmpty()) return null

    // 转化后的类型也是String类型
    if (String::class.java.isAssignableFrom(type)) {
        // 如果是字符串类型，且是以双引号开头，则可通过om.readValue转化，最终去除双引号
        return if (json.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
            om.readValue(json, type)
        } else json as T
    }

    if (json.isBlank()) return null

    // 对于复杂的类型，由于 Java 的类型擦除，泛型类型信息在运行时会丢失，因此需要通过 TypeReference 来显式保留这些信息
    return try{
        // 如果是被双引号包裹的字符串，则需要反转义后并且去除多余的双引号
        if (json.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
            om.readValue(json.unescapeJson(unescapeCount).trim('"'), type)
        } else om.readValue(json, type)
    } catch (e: Exception){
        if (type.simpleName == "Object"){
            // 如果是字符串类型，且是以双引号开头，则可通过om.readValue转化，最终去除双引号
            return if (json.matches(Regex("^\".*\"$", RegexOption.DOT_MATCHES_ALL))) {
                om.readValue(json, String::class.java) as T
            } else json as T
        }
        throw e
    }
}

inline fun <reified T> ObjectMapper.readValue(content: String): T {
    return this.readValue(content, object : TypeReference<T>() {})
}
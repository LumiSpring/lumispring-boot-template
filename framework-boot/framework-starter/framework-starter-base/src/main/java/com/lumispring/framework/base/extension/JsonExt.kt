package com.lumispring.framework.base.extension

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.EnumFeature
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
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
fun getInitObjectMapper(defaultObjectMapper: ObjectMapper? = null): JsonMapper {
    if (defaultObjectMapper is JsonMapper) {
        return defaultObjectMapper
    }

    return JsonMapper.builder()
        .addModule(getJavaTimeModule())
        .defaultDateFormat(SimpleDateFormat(DEFAULT_DATETIME_PATTERN))
        .defaultTimeZone(TimeZone.getTimeZone(DEFAULT_ZONE_ID))
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()
}

private fun getJavaTimeModule(): SimpleModule {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)
    val dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)
    val timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)
    return SimpleModule()
        .addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
        .addSerializer(LocalDate::class.java, LocalDateSerializer(dateFormatter))
        .addSerializer(LocalTime::class.java, LocalTimeSerializer(timeFormatter))
        .addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
        .addDeserializer(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
        .addDeserializer(LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
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

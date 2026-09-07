package com.lumispring.framework.base.extension

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue

/**
 * 将任意对象转换为指定类型
 * @param om 自定义ObjectMapper
 * @param unescapeCount 反转义次数，用于json字符串转成指定类型的情况
 */
inline fun <reified T> Any?.toObject(unescapeCount: Int = 0, om: ObjectMapper = DEFAULT_OBJECT_MAPPER): T? {
    if (this == null) return null
    if (this is String) return this.parseJson<T>(unescapeCount = unescapeCount, om = om)
    return om.convertValue<T>(this)
}

/**
 * 将任意对象转换为指定类型，兼容java
 * @param o 对象
 * @param type 类型
 * @param unescapeCount 反转义次数，用于json字符串转成指定类型的情况
 * @param om 自定义ObjectMapper
 */
@JvmOverloads
fun <T> toObject(o: Any?, type: Class<T>, unescapeCount: Int = 0, om: ObjectMapper = DEFAULT_OBJECT_MAPPER): T? {
    if (o == null) return null
    if (o is String) return parseJson(o, type)
    return om.convertValue(o, type)
}

/**
 * 将任意对象转换为SMap
 * @param om 自定义ObjectMapper
 * @param unescapeCount 反转义次数，用于json字符串转成指定类型的情况
 */
fun Any?.toSMap(om: ObjectMapper = DEFAULT_OBJECT_MAPPER, unescapeCount: Int = 0): SMap? {
    return this.toObject<SMap>(unescapeCount, om)
}

/**
 * 合并两个对象
 * @param other 合并对象
 */
inline fun <reified T> T?.merge(other: Any?): T? {
    if (other.isNullOrEmpty()) return this
    if (this.isNullOrEmpty() && other.isNotNullOrEmpty()) return other.toObject<T>()

    val type = T::class.java
    if (String::class.java.isAssignableFrom(type)) {
        return (this.toString() + other.toString()) as T
    }
    if (List::class.java.isAssignableFrom(type)){
        if (other is List<*>){
            return (this as List<*>).toMutableList().addAll(other) as T
        }
        return (this as List<*> + other) as T
    }
    return (this.toObject<SMap>()?.plus(other.toObject<SMap>() ?: mapOf())).toObject<T>()
}

/**
 * 逐级获取属性值，eg: { "k" : { "a":"xx", "b":"xx" } }，想要访问a，传入k,a
 */
fun Any?.getValue(vararg keys: String): Any? {
    var value: Any? = this
    keys.forEach { key ->
        if (value is Map<*,*>) value = (value as Map<*, *>)[key]
        else if (value is List<*>) value = (value as List<*>)[key.toInt()]
        else value = value.toSMap()?.get(key)
    }
    return value
}
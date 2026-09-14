package com.lumispring.framework.base.extension

import org.apache.commons.text.StringEscapeUtils

/**
 * 首字母大写
 */
fun String.firstUpperCase() :String{
    return this.first().uppercase() + this.substring(1)
}

/**
 * 首字母小写
 */
fun String.firstLowerCase() :String {
    return this.first().lowercase() + this.substring(1)
}

/**
 * 下划线转成驼峰
 */
fun String.toCamelCase() :String{
    return this.split("_").joinToString(""){
        it.firstUpperCase()
    }.firstLowerCase()
}

/**
 * 驼峰转成下划线
 */
fun String.toSnakeCase() :String{
    val snakeStr = this.split("(?=[A-Z])".toRegex()).joinToString("_"){
        it.lowercase()
    }
    return if (snakeStr.startsWith("_")) snakeStr.substring(1) else snakeStr
}

/**
 * 将Json中的字符进行反转义，去除多余的转义字符
 * @param   count   反转义次数
 */
fun String.unescapeJson(count: Int = 1): String {
    var res = this
    repeat(count){
        res = StringEscapeUtils.unescapeJson(res)
    }
    return res
}

/**
 * 对Json中的字符进行转义
 * @param   count   转义次数
 */
fun String.escapeJson(count: Int = 1): String {
    var res = this
    repeat(count){
        res = StringEscapeUtils.escapeJson(res)
    }
    return res
}

/**
 * 去除字符串中的空格
 * @param  pattern   正则表达式
 */
fun String.trimAll(pattern:String = "\\s"):String{
    return this.replace(pattern.toRegex(), "")
}

/**
 * 缩进Markdown格式
 */
fun String.indentMarkdown(): String {
    return this.trim().split("\n").joinToString("") { "${it.trim()}  \n" }
}


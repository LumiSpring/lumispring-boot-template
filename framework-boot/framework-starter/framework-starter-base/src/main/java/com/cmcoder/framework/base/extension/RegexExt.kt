package com.lumispring.framework.base.extension

/**
 * 找到正则匹配的字符
 */
fun String.findMatch(pattern: String, startIndex: Int = 0, groupIndex: Int = 1): String? {
    return pattern.toRegex().find(this, startIndex)?.groupValues?.get(groupIndex)
}

/**
 * 找到所有正则匹配的字符
 */
fun String.findAllMatch(pattern: String, startIndex: Int = 0, groupIndex: Int = 1): List<String> {
    return pattern.toRegex().findAll(this, startIndex).map { it.groupValues[groupIndex] }.toList()
}
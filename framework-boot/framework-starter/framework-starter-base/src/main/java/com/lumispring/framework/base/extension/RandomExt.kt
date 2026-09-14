package com.lumispring.framework.base.extension

import java.util.*

const val LETTER_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val NUMS = "0123456789"
private const val NUM_CHARS = LETTER_CHARS + NUMS

// 随机数生成器，在多线程环境中，SplittableRandom的性能明显优于Random
private val RAND = SplittableRandom()

/**
 * 随机数
 * @param start 开始
 * @param end 结束
 */
fun randomInt(start: Int, end: Int): Int {
    return RAND.ints(1, start, end + 1).findFirst().asInt
}

/**
 * 随机数
 * @param max 最大值
 * @param includeZero 是否包含0
 */
fun randomInt(max: Int, includeZero: Boolean = true): Int {
    val start = if (includeZero) 0 else 1
    return RAND.ints(1, start, max + 1).findFirst().asInt
}

/**
 * 随机字符串
 * @param len 长度
 * @param includeNum 是否包含数字
 */
fun randomString(len: Int, includeNum: Boolean = true): String {
    val chars = (if (includeNum) NUM_CHARS else LETTER_CHARS).toList()
    return (0 until len).map { chars[RAND.nextInt(chars.size)] }.joinToString("")
}

/**
 * 随机UUID，随机生成32位字符串
 * @param includeSeparator 是否包含分隔符
 */
fun randomUuid(includeSeparator:Boolean = false):String{
    return UUID.randomUUID().toString().let {
        if(includeSeparator) it else it.replace("-", "")
    }
}
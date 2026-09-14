package com.lumispring.framework.base.extension

import com.lumispring.framework.base.model.ErrorCode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs


const val DEFAULT_DATE_PATTERN = "yyyy-MM-dd"
const val DEFAULT_TIME_PATTERN = "HH:mm:ss"
const val DEFAULT_DATETIME_PATTERN = "$DEFAULT_DATE_PATTERN $DEFAULT_TIME_PATTERN"

//默认上海，不要用系统默认（有可能服务器不是东八区）
val DEFAULT_ZONE_ID:ZoneId = ZoneId.of("Asia/Shanghai")

/**
 * 场景的日期格式
 */
private val DATE_PATTERNS = arrayOf(
    "yyyy-MM-dd HH:mm:ss",
    "yyyy/MM/dd HH:mm:ss",
    "yyyy/M/d HH:mm:ss",
    "yyyy/MM/dd HH:mm",
    "yyyy/M/dd HH:mm",
    "yyyy/M/dd HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm'Z'",
    "E MMM dd HH:mm:ss z yyyy",//Date格式
    "yy-M-d HH:mm",
    "yy-M-d",
    "yyyy-MM-dd",
    "yyyy-M-d",
    "yyyy/MM/dd",
    "yyyy年M月d日",
    "yyyy年MM月dd日",
    "yyyy/M/dd",
    "yyyyMMdd",
    "yyyy/M/d",
    "MM/dd/yyyy",
    "M/d/yy",
    "EEE, MMM d, yyyy",
    "yyyy年M月",
    "yyyy-MM",
    "yyyy/MM",
    "yyyyMM",
    "yyMd",
)

/**
 * 场景的日期时间格式
 */
private val LOCAL_DATE_TIME_PATTERNS = arrayOf(
    "yyyy-MM-dd HH:mm:ss.SSSSSS",
    "yyyy-MM-dd HH:mm:ss.SSS",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-M-d HH:mm:ss.SSSSSS",
    "yyyy-M-d HH:mm:ss.SSS",
    "yyyy-M-d HH:mm:ss",
    "yyyy/MM/dd HH:mm:ss.SSSSSS",
    "yyyy/MM/dd HH:mm:ss.SSS",
    "yyyy/MM/dd HH:mm:ss",
    "yyyy/M/d HH:mm:ss.SSSSSS",
    "yyyy/M/d HH:mm:ss.SSS",
    "yyyy/M/d HH:mm:ss",
    "yyyy年MM月dd日 HH时mm分ss秒SSS毫秒",
    "yyyy年MM月dd日 HH时mm分ss秒",
    "yyyy年M月d日 HH时mm分ss秒SSS毫秒",
    "yyyy年M月d日 HH时mm分ss秒",
)

/**
 * 场景的日期时间格式，带T
 */
private val LOCAL_DATE_TIME_PATTERNS_T = arrayOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy/MM/dd'T'HH:mm:ss.SSSSSSSSS",
    "yyyy/MM/dd'T'HH:mm:ss.SSSSSS",
    "yyyy/MM/dd'T'HH:mm:ss.SSS",
    "yyyy/MM/dd'T'HH:mm:ss.SSS'Z'",
    "yyyy/MM/dd'T'HH:mm:ss",
    "yyyy-M-d'T'HH:mm:ss.SSSSSSSSS",
    "yyyy-M-d'T'HH:mm:ss.SSSSSS",
    "yyyy-M-d'T'HH:mm:ss.SSS",
    "yyyy-M-d'T'HH:mm:ss.SSS'Z'",
    "yyyy/M/d'T'HH:mm:ss.SSSSSSSSS",
    "yyyy/M/d'T'HH:mm:ss.SSSSSS",
    "yyyy/M/d'T'HH:mm:ss.SSS",
    "yyyy/M/d'T'HH:mm:ss.SSS'Z'",
)

/**
 * 获取当前时间
 */
fun now(): LocalDateTime {
    return LocalDateTime.now()
}

/**
 * 格式化时间
 * @param pattern 时间格式，默认为"yyyy-MM-dd HH:mm:ss"
 */
fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * 获取当前时间的字符串形式
 * @param pattern 时间格式，默认为"yyyy-MM-dd HH:mm:ss"
 */
fun nowFormat(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return now().format(pattern)
}

/**
 * 获取当前时间的字符串形式
 * @param formatter 时间格式
 */
fun nowFormat(formatter: DateTimeFormatter): String {
    return now().format(formatter)
}

/**
 * 获取当前日期
 * @param plusDay 加减天数，默认为0
 */
fun nowDate(plusDay: Long = 0): LocalDate {
    return LocalDate.now().plusDays(plusDay)
}

/**
 * 格式化日期
 * @param pattern 日期格式，默认为"yyyy-MM-dd"
 */
fun LocalDate.format(pattern: String = "yyyy-MM-dd"): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * 获取当前日期的字符串形式
 * @param pattern 日期格式，默认为"yyyy-MM-dd"
 */
fun nowDateFormat(pattern: String = "yyyy-MM-dd"): String {
    return nowDate().format(pattern)
}

/**
 * 获取当前日期的字符串形式
 * @param formatter 日期格式
 */
fun nowDateFormat(formatter: DateTimeFormatter): String {
    return nowDate().format(formatter)
}

/**
 * 将时间戳转换为Date
 */
fun Long.toDate(): Date {
    return Date(this)
}

/**
 * 将字符串转换为Date
 */
fun String.toDate(zoneId: ZoneId = DEFAULT_ZONE_ID): Date {
    this.checkNotEmpty("时间参数不能为空", ErrorCode.SERVICE_PARAM_FORMAT_ERROR)

    // 如果是纯数字，可能是时间戳，10位是秒，13位是毫秒
    if (this.contains(Regex("^\\d{10}"))) {
        return this.toLong().toDate()
    }

    // 轮询所有的日期格式，找到合适的格式
    val newPatterns = DATE_PATTERNS.sortedBy { abs(it.length - this.length) }
    for (pattern in newPatterns) {
        try {
            return SimpleDateFormat(pattern).apply {
                this.timeZone = TimeZone.getTimeZone(zoneId)
            }.parse(this)
        } catch (e: Exception) {
            continue
        }
    }
    throw ErrorCode.SERVICE_PARAM_FORMAT_ERROR.exception(log="时间参数格式错误：$this")
}

/**
 * 将时间戳转换为LocalDateTime
 */
@JvmOverloads
fun Long.toLocalDateTime(zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
}

/**
 * 将Date转换为LocalDateTime
 */
@JvmOverloads
fun Date.toLocalDateTime(zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTime {
    //兼容sql Date情况
    if (this is java.sql.Date) {
        return this.time.toLocalDateTime()
    }
    return this.toInstant().atZone(zoneId).toLocalDateTime()
}

/**
 * 将字符串转换为LocalDateTime
 */
@JvmOverloads
fun String.toLocalDateTime(zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTime {
    this.checkNotEmpty("时间参数不能为空", ErrorCode.SERVICE_PARAM_FORMAT_ERROR)

    // 如果是纯数字，可能是时间戳，10位是秒，13位是毫秒
    if (this.contains(Regex("^\\d{10}"))) {
        return this.toLong().toLocalDateTime(zoneId)
    }

    // 轮询所有的日期格式，找到合适的格式
    val newPatterns = if (this.contains(Regex("[tTzZ]"))){
        LOCAL_DATE_TIME_PATTERNS_T.sortedBy { abs(it.trimAll("\'[TZ]\'").length - this.trimAll("[tTzZ]").length) }
    } else {
        LOCAL_DATE_TIME_PATTERNS.sortedBy { abs(it.length - this.length) }
    }
    for (pattern in newPatterns) {
        try {
            return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
        } catch (e: Exception) {
            continue
        }
    }

    return this.toDate(zoneId).toLocalDateTime(zoneId)
}

/**
 * 将Date转换为LocalDate
 */
fun Date.toLocalDate(): LocalDate {
    return this.toLocalDateTime().toLocalDate()
}

/**
 * 将时间戳转换为LocalDate
 */
fun Long.toLocalDate(): LocalDate {
    return this.toLocalDateTime().toLocalDate()
}

/**
 * 将字符串转换为LocalDate
 */
fun String.toLocalDate(): LocalDate {
    return this.toDate().toLocalDate()
}

/**
 * 将Date转换为时间戳
 */
fun Date.toTimeStamp(): Long {
    return this.time
}

/**
 * 将LocalDateTime转换为时间戳
 */
@JvmOverloads
fun LocalDateTime.toTimestamp(zoneId: ZoneId = DEFAULT_ZONE_ID): Long {
    return this.atZone(zoneId).toInstant().toEpochMilli()
}

/**
 * 将LocalDate转换为时间戳
 */
@JvmOverloads
fun LocalDate.toTimeStamp(zoneId: ZoneId = DEFAULT_ZONE_ID): Long {
    return this.atStartOfDay(zoneId).toInstant().toEpochMilli()
}

/**
 * 获取当前月份的最大日期
 */
fun maxDateOfMonth(): LocalDate {
    return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
}

/**
 * 获取指定日期的当月最大日期
 */
@JvmName("maxDateOfMonth1")
fun LocalDate.maxDateOfMonth(): LocalDate {
    return this.withDayOfMonth(this.lengthOfMonth())
}

/**
 * 获取当前月份的最小日期
 */
fun minDateOfMonth(): LocalDate {
    return LocalDate.now().withDayOfMonth(1)
}

/**
 * 获取指定日期的当月最小日期
 */
@JvmName("minDateOfMonth1")
fun LocalDate.minDateOfMonth(): LocalDate {
    return this.withDayOfMonth(1)
}

/**
 * 将LocalDate转换为Date
 */
fun LocalDate.toDate(): Date{
    return Date.from(this.atStartOfDay(DEFAULT_ZONE_ID).toInstant())
}

/**
 * 将LocalDateTime转换为Date
 */
fun LocalDateTime.toDate(): Date{
    return Date.from(this.atZone(DEFAULT_ZONE_ID).toInstant())
}
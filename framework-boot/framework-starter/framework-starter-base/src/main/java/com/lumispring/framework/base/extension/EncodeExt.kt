package com.lumispring.framework.base.extension

import org.apache.hc.core5.http.message.BasicNameValuePair
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.Collection

val UTF8 = Charsets.UTF_8
val DEFAULT_CHARSET = UTF8

fun String.base64Encode(): String = Base64.getEncoder().encodeToString(this.toByteArray())
fun ByteArray.base64Encode2Str(charset: Charset = Charsets.UTF_8): String =
    String(Base64.getEncoder().encode(this), charset)

fun ByteArray.base64Encode(): ByteArray = Base64.getEncoder().encode(this)

fun String.base64Decode(): ByteArray = Base64.getDecoder().decode(this)
fun ByteArray.base64Decode2Str(charset: Charset = Charsets.UTF_8): String =
    String(Base64.getDecoder().decode(this), charset)

fun ByteArray.base64Decode(): ByteArray = Base64.getDecoder().decode(this)

fun String.urlEncode(charset: String = "UTF-8"): String = URLEncoder.encode(this, charset)
fun String.urlDecode(charset: String = "UTF-8"): String = URLDecoder.decode(this, charset)

val BASE64_REGEX = Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")
const val BASE64_SPLIITOR = "____"
fun String.isBase64(): Boolean {
    return this.split(BASE64_SPLIITOR)[0].matches(BASE64_REGEX)
}

fun String.urlToBase64Str(): String? {
    try {
        return URL(this).readBytes().base64Encode2Str()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun String.urlToEncodeUrl(): String {
    try {
        val url = URL(this)
        return URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref).toString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return this;
}

fun Collection<BasicNameValuePair>.toUrlEncode(): String{
    return this.joinToString("&") {
        "${URLEncoder.encode(it.name, Charsets.UTF_8)}=${URLEncoder.encode(it.value, Charsets.UTF_8)}"
    }
}

fun ByteArray.gzip(): ByteArray {
    val out = ByteArrayOutputStream()
    val gzipOut = GZIPOutputStream(out)
    gzipOut.write(this)
    gzipOut.close()
    return out.toByteArray()
}

fun ByteArray.unGzip(): ByteArray {
    val gzipIn = this.inputStream()
    val isGzipStream = this.isGzipFormat()
    if (isGzipStream) {
        ByteArrayOutputStream().use {
            val ungzip = GZIPInputStream(gzipIn)
            val buffer = ByteArray(256)
            var len = ungzip.read(buffer)
            while (len >= 0) {
                it.write(buffer, 0, len)
                len = ungzip.read(buffer)
            }
            return it.toByteArray()
        }
    } else {
        return this
    }
}

/**
 * 是否是gzip格式
 */
fun ByteArray.isGzipFormat(): Boolean {
    return this[0] == GZIPInputStream.GZIP_MAGIC.toByte() && this[1] == (GZIPInputStream.GZIP_MAGIC shr 8).toByte()
}


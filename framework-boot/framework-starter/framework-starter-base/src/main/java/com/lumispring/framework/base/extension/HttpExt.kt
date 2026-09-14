package com.lumispring.framework.base.extension

import com.lumispring.framework.base.model.ErrorCode
import org.apache.hc.client5.http.HttpRequestRetryStrategy
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.entity.mime.ContentBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.entity.mime.MultipartPart
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient
import org.apache.hc.client5.http.impl.async.HttpAsyncClients
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.core5.concurrent.FutureCallback
import org.apache.hc.core5.http.*
import org.apache.hc.core5.http.io.entity.*
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder
import org.apache.hc.core5.http.message.BasicHeader
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.apache.hc.core5.http.nio.*
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityConsumer
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers
import org.apache.hc.core5.http.nio.support.AbstractAsyncResponseConsumer
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder
import org.apache.hc.core5.http.protocol.HttpContext
import org.apache.hc.core5.http.support.AbstractRequestBuilder
import org.apache.hc.core5.net.URIBuilder
import org.apache.hc.core5.util.ByteArrayBuffer
import org.apache.hc.core5.util.Timeout
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

enum class HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE
}

private val logger = logger()

// 创建连接池
fun createHttpPoolConnectionManager(): PoolingHttpClientConnectionManager{
    return PoolingHttpClientConnectionManagerBuilder.create().apply {
        setDefaultConnectionConfig(ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(600000L)) // 连接超时，10分钟
            .setSocketTimeout(Timeout.ofMilliseconds(600000L))  // 响应超时时间，10分钟
            .build())
        setMaxConnTotal(500) // 最大连接数
        setMaxConnPerRoute(20) // 设置每个目标主机的最大连接数。
    }.build()
}

// 创建异步连接池
fun createHttpPoolAsyncConnectionManager():PoolingAsyncClientConnectionManager {
    return PoolingAsyncClientConnectionManagerBuilder.create().apply {
        setDefaultConnectionConfig(
            ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(600000L)) // 连接超时，10分钟
                .setSocketTimeout(Timeout.ofMilliseconds(600000L))  // 响应超时时间，10分钟
                .build()
        )
        setMaxConnTotal(500) // 最大连接数
        setMaxConnPerRoute(20) // 设置每个目标主机的最大连接数。
    }.build()
}

// 默认连接池
val DEFAULT_POOL_HTTP_CLIENT_CONNECTION_MANAGER by lazy {
    getBeanOrNull(PoolingHttpClientConnectionManager::class.java) ?: createHttpPoolConnectionManager()
}

// 默认异步连接池
val DEFAULT_POOL_HTTP_ASYNC_CLIENT_CONNECTION_MANAGER by lazy {
    getBeanOrNull(PoolingAsyncClientConnectionManager::class.java) ?: createHttpPoolAsyncConnectionManager()
}

// 创建Http客户端
fun createHttpClient(): CloseableHttpClient {
    return HttpClients.custom()
        .setConnectionManager(DEFAULT_POOL_HTTP_CLIENT_CONNECTION_MANAGER)
        .setRetryStrategy(createRetryStrategy())
        .build()
}


// Http重试策略
private fun createRetryStrategy() : HttpRequestRetryStrategy {
    return DefaultHttpRequestRetryStrategy()
}


// 创建异步Http客户端
fun createHttpAsyncClient(): CloseableHttpAsyncClient {
    return HttpAsyncClients.custom()
        .setRetryStrategy(createRetryStrategy())
        .setConnectionManager(DEFAULT_POOL_HTTP_ASYNC_CLIENT_CONNECTION_MANAGER)
        .build()
}

// 默认的请求头
private val DEFAULT_HTTP_REQUEST_HEADER = mapOf<String, String>(
//    HttpHeaders.CONTENT_TYPE to ContentType.APPLICATION_JSON.toString(),
)

// 默认的Http客户端
private val DEFAULT_HTTP_CLIENT by lazy {
    getBeanOrNull("iHttpClient", CloseableHttpClient::class.java) ?: createHttpClient()
}

// 默认的异步Http客户端
private val DEFAULT_HTTP_ASYNC_CLIENT by lazy {
    (getBeanOrNull("iHttpAsyncClient", CloseableHttpAsyncClient::class.java)
        ?: createHttpAsyncClient()).also { it.start() }
}

// 代理Http客户端缓存
private val PROXY_HTTP_CLIENT_CACHE =
    com.lumispring.framework.base.designpattern.DefaultCache<String, CloseableHttpClient>(expireAfterAccess = 60)

// 代理Http异步客户端缓存
private val PROXY_HTTP_ASYNC_CLIENT_CACHE =
    com.lumispring.framework.base.designpattern.DefaultCache<String, CloseableHttpAsyncClient>(expireAfterAccess = 60)

/**
 * http请求响应结果
 * @param body 响应体
 * @param contentType 响应类型
 * @param contentLength 响应长度
 * @param contentEncoding 响应编码
 * @param isStreaming 是否流式
 * @param isRepeatable 是否可重复
 * @param headers 响应头
 * @param code 响应状态码
 * @param version http版本
 * @param reasonPhrase 响应原因
 *
 */
data class HttpResponse(
    var body: ByteArray? = null,
    var contentType: String? = null,
    var contentLength: Long? = null,
    var contentEncoding: String? = null,
    var isStreaming: Boolean? = null,
    var isRepeatable: Boolean? = null,
    var headers: Map<String, String>? = null,
    var code: Int? = null,
    var version: String? = null,
    var reasonPhrase: String? = null
) {
    /**
     * 获取文本格式的响应体
     */
    fun text() = body?.toString(Charsets.UTF_8)

    /**
     * 获取json格式的响应体
     */
    fun json(): SMap? {
        return body?.toString(Charsets.UTF_8)?.toObject<SMap>()
    }

    inline fun <reified T> json(): T? {
        return body?.toString(Charsets.UTF_8)?.toObject<T>()
    }

    inline fun <reified T> data(): T? {
        return json()?.get("data")?.toObject<T>()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (contentLength != other.contentLength) return false
        if (isStreaming != other.isStreaming) return false
        if (isRepeatable != other.isRepeatable) return false
        if (code != other.code) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false
        if (contentType != other.contentType) return false
        if (contentEncoding != other.contentEncoding) return false
        if (headers != other.headers) return false
        if (version != other.version) return false
        if (reasonPhrase != other.reasonPhrase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentLength?.hashCode() ?: 0
        result = 31 * result + (isStreaming?.hashCode() ?: 0)
        result = 31 * result + (isRepeatable?.hashCode() ?: 0)
        result = 31 * result + (code ?: 0)
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (contentEncoding?.hashCode() ?: 0)
        result = 31 * result + (headers?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (reasonPhrase?.hashCode() ?: 0)
        return result
    }
}

/**
 * 获取HttpClient
 */
fun getHttpClient(proxy: HttpHost? = null): CloseableHttpClient {
    return if (proxy.isNullOrEmpty()) DEFAULT_HTTP_CLIENT
    else PROXY_HTTP_CLIENT_CACHE.getOrPut(proxy.toHostString()) {
        HttpClients.custom().let { httpClientBuilder ->
            httpClientBuilder.setProxy(proxy)
            httpClientBuilder.setRetryStrategy(createRetryStrategy())
            httpClientBuilder.build()
        }
    }
}

/**
 * 获取HttpAsyncClient
 */
fun getHttpAsyncClient(proxy: HttpHost? = null): CloseableHttpAsyncClient {
    return if (proxy.isNullOrEmpty()) DEFAULT_HTTP_ASYNC_CLIENT
    else PROXY_HTTP_ASYNC_CLIENT_CACHE.getOrPut(proxy.toHostString()) {
        HttpAsyncClients.custom().let { httpClientBuilder ->
            httpClientBuilder.setProxy(proxy)
            httpClientBuilder.setRetryStrategy(createRetryStrategy())
            httpClientBuilder.build()
        }
    }
}

internal fun <T> AbstractRequestBuilder<T>.generateUri(
    url: String,
    method: HttpMethod,
    data: Any? = null,
): URI {
    return URIBuilder(URI(url)).let { uriBuilder ->
        if (data.isNotNullOrEmpty() && method in listOf(HttpMethod.GET, HttpMethod.DELETE)) {
            // get，delete请求通过url传递参数
            if (data is Map<*, *>) {
                data.forEach { (k, v) ->
                    when (v) {
                        is String -> this.addParameter(k.toString(), v)
                        is List<*> -> v.forEach { this.addParameter(k.toString(), it.toString()) }
                        else -> this.addParameter(k.toString(), v.toString())
                    }
                }
            } else if (data is String) {
                uriBuilder.setCustomQuery(data.toString())
            } else {
                val dataMap = data.toSMap()
                dataMap?.forEach { (k, v) ->
                    when (v) {
                        is String -> this.addParameter(k, v)
                        is List<*> -> v.forEach { this.addParameter(k, it.toString()) }
                        else -> this.addParameter(k, v.toString())
                    }
                }
            }
        }
        uriBuilder.build()
    }
}

/**
 * 构建请求
 * @param url 请求地址
 * @param method 请求方法
 * @param data 请求数据
 * @param headers 请求头
 */
internal fun generateClassicRequest(
    url: String,
    method: HttpMethod,
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
): ClassicHttpRequest {
    return ClassicRequestBuilder.create(method.name).let { requestBuilder ->
        // 处理url，如果是get/delete则通过url传递参数
        requestBuilder.setUri(requestBuilder.generateUri(url, method, data))
        // 合并默认请求头
        val mergeHeader = (DEFAULT_HTTP_REQUEST_HEADER + headers).toMutableMap()

        // 构建请求体
        // post，put请求通过请求体传递参数
        if (data.isNotNullOrEmpty() && method in listOf(HttpMethod.POST, HttpMethod.PUT)) {
            // form表单传参
            if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_FORM_URLENCODED.mimeType) == true) {
                if (data is Map<*, *>) {
                    requestBuilder.setEntity(UrlEncodedFormEntity(mutableListOf<BasicNameValuePair>().apply {
                        data.forEach { (k, v) ->
                            when (v) {
                                is String -> add(BasicNameValuePair(k.toString(), v))
                                is List<*> -> v.forEach { add(BasicNameValuePair(k.toString(), it.toString())) }
                                else -> add(BasicNameValuePair(k.toString(), v.toString()))
                            }
                        }
                    }))
                } else if (data is String) {
                    requestBuilder.setEntity(StringEntity(data.toString(), ContentType.APPLICATION_FORM_URLENCODED))
                } else {
                    val dataMap = data.toObject<SMap>()
                    requestBuilder.setEntity(UrlEncodedFormEntity(mutableListOf<BasicNameValuePair>().apply {
                        dataMap?.forEach { (k, v) ->
                            when (v) {
                                is String -> add(BasicNameValuePair(k, v))
                                is List<*> -> v.forEach { add(BasicNameValuePair(k, it.toString())) }
                                else -> add(BasicNameValuePair(k, v.toString()))
                            }
                        }
                    }))
                }
                // 携带文件的form表单
            } else if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.MULTIPART_FORM_DATA.mimeType) == true) {
                val httpEntity = MultipartEntityBuilder.create().let { multipartEntityBuilder ->
                    if (data is Map<*, *>) {
                        data.forEach { (k, v) ->
                            when (v) {
                                is String -> multipartEntityBuilder.addTextBody(k.toString(), v)
                                is List<*> -> v.forEach {
                                    multipartEntityBuilder.addTextBody(
                                        k.toString(),
                                        it.toString()
                                    )
                                }

                                is InputStream -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is ByteArray -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is File -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is MultipartPart -> multipartEntityBuilder.addPart(v)
                                is ContentBody -> multipartEntityBuilder.addPart(k.toString(), v)
                                else -> throw ErrorCode.REQUEST_PARAM_ERROR.exception()
                            }
                        }
                    } else if (data is String) {
                        data.split("&").forEach {
                            val split = it.split("=")
                            multipartEntityBuilder.addTextBody(split[0], split[1])
                        }
                    } else if (data is MultipartPart) {
                        multipartEntityBuilder.addPart(data)
                    } else {
                        val dataMap = data.toObject<SMap>()
                        dataMap?.forEach { (k, v) ->
                            when (v) {
                                is String -> multipartEntityBuilder.addTextBody(k, v)
                                is List<*> -> v.forEach { multipartEntityBuilder.addTextBody(k, it.toString()) }
                                is InputStream -> multipartEntityBuilder.addBinaryBody(k, v)
                                is ByteArray -> multipartEntityBuilder.addBinaryBody(k, v)
                                is File -> multipartEntityBuilder.addBinaryBody(k, v)
                                is MultipartPart -> multipartEntityBuilder.addPart(v)
                                is ContentBody -> multipartEntityBuilder.addPart(k, v)
                                else -> throw ErrorCode.REQUEST_PARAM_ERROR.exception()
                            }
                        }
                    }
                    multipartEntityBuilder.build()
                }
                requestBuilder.setEntity(httpEntity)
                // 使用流传递二进制数据
            } else if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_OCTET_STREAM.mimeType) == true) {
                val httpEntity = when (data) {
                    is ByteArray -> HttpEntities.create(data, ContentType.APPLICATION_OCTET_STREAM)
                    is InputStream -> HttpEntities.create(
                        data.readAllBytes(),
                        ContentType.APPLICATION_OCTET_STREAM
                    )

                    is File -> HttpEntities.create(data.readBytes(), ContentType.APPLICATION_OCTET_STREAM)
                    else -> HttpEntities.create(
                        data.toInputStream().readAllBytes(),
                        ContentType.APPLICATION_OCTET_STREAM
                    )
                }
                requestBuilder.setEntity(httpEntity)
            } else {
                // 如果是post/put请求且未指定Content-Type时，根据传入的data类型添加请求体
                when (data) {
                    is String -> {
                        if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_JSON.mimeType) == true) requestBuilder.setEntity(
                            StringEntity(data, ContentType.APPLICATION_JSON)
                        )
                        else requestBuilder.setEntity(StringEntity(data, ContentType.APPLICATION_FORM_URLENCODED))
                    }

                    is ByteArray -> requestBuilder.setEntity(
                        ByteArrayEntity(
                            data,
                            ContentType.APPLICATION_OCTET_STREAM
                        )
                    )

                    is InputStream -> requestBuilder.setEntity(
                        InputStreamEntity(
                            data,
                            ContentType.APPLICATION_OCTET_STREAM
                        )
                    )

                    is File -> requestBuilder.setEntity(
                        FileEntity(
                            data,
                            ContentType.APPLICATION_OCTET_STREAM
                        )
                    )

                    is MultipartPart -> requestBuilder.setEntity(
                        MultipartEntityBuilder.create().let {
                            it.addPart(data)
                            it.setContentType(ContentType.MULTIPART_FORM_DATA)
                            it.build()
                        }
                    )

                    is List<*> -> {
                        if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_JSON.mimeType) == true) {
                            requestBuilder.setEntity(StringEntity(data.toJsonString(), ContentType.APPLICATION_JSON))
                        } else {
                            requestBuilder.setEntity(
                                UrlEncodedFormEntity(data.map {
                                    if (it is Pair<*, *>) BasicNameValuePair(it.first.toString(), it.second.toString())
                                    else if (it is NameValuePair) BasicNameValuePair(it.name, it.value)
                                    else throw ErrorCode.REQUEST_PARAM_ERROR.exception(log = "请求体传入list时，元素仅支持Pair<String,String>或NameValuePair类型")
                                })
                            )
                        }
                    }

                    else -> {
                        requestBuilder.setEntity(StringEntity(data.toJsonString(), ContentType.APPLICATION_JSON))
                    }
                }
            }
        }
        // 设置请求头
        mergeHeader.forEach { (k, v) -> requestBuilder.setHeader(BasicHeader(k, v)) }
        requestBuilder.build()
    }
}

private val responseCache = com.lumispring.framework.base.designpattern.DefaultCache<String, HttpResponse>(
    maxCapacity = 1000,
    expireAfterAccess = 5,
    timeUnit = TimeUnit.MINUTES
)

/**
 * http请求
 * @param method 请求方法
 * @param data 请求数据
 * @param headers 请求头
 * @param proxies 代理池，会随机选择一个代理
 * @param cache 是否缓存请求结果，默认不缓存
 */
fun String.doRequest(
    method: HttpMethod,
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    cache: Boolean = false
): HttpResponse {
    // cache为true，获取缓存请求结果
    if (proxies.isNullOrEmpty() && cache) {
        val md5 = "${method}${data}${headers}".md5()
        if (responseCache.contains(md5)) {
            return responseCache[md5]!!
        }
    }

    try {
        val httpClient = getHttpClient(proxies?.random())
        // 构建请求
        val request = generateClassicRequest(this, method, data, headers)
        // 处理请求结果
        return httpClient.execute(request) { res ->
            res.use {
                HttpResponse(
                    contentType = res.entity.contentType,
                    contentLength = res.entity.contentLength,
                    contentEncoding = res.entity.contentEncoding,
                    isStreaming = res.entity.isStreaming,
                    isRepeatable = res.entity.isRepeatable,
                    headers = res.headers.associate { it.name to it.value },
                    code = res.code,
                    version = res.version.toString(),
                    reasonPhrase = res.reasonPhrase,
                    body = EntityUtils.toByteArray(res.entity),
                )   // 确保内容被消费
                    .also { EntityUtils.consume(res.entity) }
                    .also { if (proxies.isNullOrEmpty() && cache) responseCache.put("${method}${data}${headers}".md5(), it) }
                    .also { logger.info("url:[$this] method:[$method] status:[${it.code}] response:${it.text()}") }
            }
        }
    } catch (e: Exception) {
        logger.error("url:[$this] method:[$method] error:[${e.message}]")
        e.printStackTrace()
        throw e
    }
}

fun String.doGet(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    cache: Boolean = false
) =
    doRequest(HttpMethod.GET, data, headers, proxies, cache)

fun String.doPost(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    cache: Boolean = false
) =
    doRequest(HttpMethod.POST, data, headers, proxies, cache)

fun String.doPut(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    cache: Boolean = false
) =
    doRequest(HttpMethod.PUT, data, headers, proxies, cache)

fun String.doDelete(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    cache: Boolean = false
) =
    doRequest(HttpMethod.DELETE, data, headers, proxies, cache)


/**
 * 构建请求
 * @param url 请求地址
 * @param method 请求方法
 * @param data 请求数据
 * @param headers 请求头
 */
internal fun generateAsyncRequest(
    url: String,
    method: HttpMethod,
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
): AsyncRequestProducer {
    return AsyncRequestBuilder.create(method.name).let { requestBuilder ->
        // 处理url，如果是get/delete则通过url传递参数
        requestBuilder.setUri(requestBuilder.generateUri(url, method, data))
        val mergeHeader = DEFAULT_HTTP_REQUEST_HEADER + headers

        // post，put请求通过请求体传递参数
        if (data.isNotNullOrEmpty() && method in listOf(HttpMethod.POST, HttpMethod.PUT)) {
            // form表单传参
            if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_FORM_URLENCODED.mimeType) == true) {
                if (data is Map<*, *>) {
                    AsyncEntityProducers.createUrlEncoded(mutableListOf<BasicNameValuePair>().apply {
                        data.forEach { (k, v) ->
                            when (v) {
                                is String -> add(BasicNameValuePair(k.toString(), v))
                                is List<*> -> v.forEach { add(BasicNameValuePair(k.toString(), it.toString())) }
                                else -> add(BasicNameValuePair(k.toString(), v.toString()))
                            }
                        }
                    }, Charsets.UTF_8)
                } else if (data is String) {
                    requestBuilder.setEntity(data, ContentType.APPLICATION_FORM_URLENCODED)
                } else {
                    val dataMap = data.toObject<SMap>()
                    AsyncEntityProducers.createUrlEncoded(mutableListOf<BasicNameValuePair>().apply {
                        dataMap?.forEach { (k, v) ->
                            when (v) {
                                is String -> add(BasicNameValuePair(k, v))
                                is List<*> -> v.forEach { add(BasicNameValuePair(k, it.toString())) }
                                else -> add(BasicNameValuePair(k, v.toString()))
                            }
                        }
                    }, Charsets.UTF_8)
                }
                // 携带文件的form表单
            } else if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.MULTIPART_FORM_DATA.mimeType) == true) {
                val httpEntity = MultipartEntityBuilder.create().let { multipartEntityBuilder ->
                    if (data is Map<*, *>) {
                        data.forEach { (k, v) ->
                            when (v) {
                                is String -> multipartEntityBuilder.addTextBody(k.toString(), v)
                                is List<*> -> v.forEach {
                                    multipartEntityBuilder.addTextBody(
                                        k.toString(),
                                        it.toString()
                                    )
                                }

                                is InputStream -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is ByteArray -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is File -> multipartEntityBuilder.addBinaryBody(k.toString(), v)
                                is MultipartPart -> multipartEntityBuilder.addPart(v)
                                else -> throw ErrorCode.REQUEST_PARAM_ERROR.exception()
                            }
                        }
                    } else if (data is String) {
                        data.split("&").forEach {
                            val split = it.split("=")
                            multipartEntityBuilder.addTextBody(split[0], split[1])
                        }
                    } else {
                        val dataMap = data.toObject<SMap>()
                        dataMap?.forEach { (k, v) ->
                            when (v) {
                                is String -> multipartEntityBuilder.addTextBody(k, v)
                                is List<*> -> v.forEach { multipartEntityBuilder.addTextBody(k, it.toString()) }
                                is InputStream -> multipartEntityBuilder.addBinaryBody(k, v)
                                is ByteArray -> multipartEntityBuilder.addBinaryBody(k, v)
                                is File -> multipartEntityBuilder.addBinaryBody(k, v)
                                is MultipartPart -> multipartEntityBuilder.addPart(v)
                                is ContentBody -> multipartEntityBuilder.addPart(k, v)
                                else -> throw ErrorCode.REQUEST_PARAM_ERROR.exception()
                            }
                        }
                    }
                    multipartEntityBuilder.build()
                }
                requestBuilder.setEntity(
                    AsyncEntityProducers.create(
                        httpEntity.content.readAllBytes(),
                        ContentType.MULTIPART_FORM_DATA
                    )
                )
                // 使用流传递二进制数据
            } else if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_OCTET_STREAM.mimeType) == true) {
                when (data) {
                    is ByteArray -> requestBuilder.setEntity(data, ContentType.APPLICATION_OCTET_STREAM)
                    is InputStream -> requestBuilder.setEntity(
                        data.readAllBytes(),
                        ContentType.APPLICATION_OCTET_STREAM
                    )

                    is File -> requestBuilder.setEntity(data.readBytes(), ContentType.APPLICATION_OCTET_STREAM)
                    else -> throw ErrorCode.REQUEST_PARAM_ERROR.exception()
                }
                // json传参
            } else {
                // 如果是post/put请求且未指定Content-Type时，根据传入的data类型添加请求体
                when (data) {
                    is String -> {
                        if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_JSON.mimeType) == true) requestBuilder.setEntity(
                            data,
                            ContentType.APPLICATION_JSON
                        )
                        else requestBuilder.setEntity(data, ContentType.APPLICATION_FORM_URLENCODED)
                    }

                    is ByteArray -> requestBuilder.setEntity(data, ContentType.APPLICATION_OCTET_STREAM)
                    is InputStream -> requestBuilder.setEntity(
                        data.readAllBytes(),
                        ContentType.APPLICATION_OCTET_STREAM
                    )

                    is File -> requestBuilder.setEntity(data.readBytes(), ContentType.APPLICATION_OCTET_STREAM)
                    is MultipartPart -> requestBuilder.setEntity(
                        MultipartEntityBuilder.create().let {
                            it.addPart(data)
                            it.setContentType(ContentType.MULTIPART_FORM_DATA)
                            it.build()
                        }.content.readAllBytes(), ContentType.MULTIPART_FORM_DATA
                    )

                    is List<*> -> {
                        if (mergeHeader[HttpHeaders.CONTENT_TYPE]?.contains(ContentType.APPLICATION_JSON.mimeType) == true) {
                            requestBuilder.setEntity(data.toJsonString(), ContentType.APPLICATION_JSON)
                        } else {
                            requestBuilder.setEntity(
                                AsyncEntityProducers.createUrlEncoded(data.map {
                                    if (it is Pair<*, *>) BasicNameValuePair(it.first.toString(), it.second.toString())
                                    else if (it is NameValuePair) BasicNameValuePair(it.name, it.value)
                                    else throw ErrorCode.REQUEST_PARAM_ERROR.exception(log = "请求体传入list时，元素仅支持Pair<String,String>或NameValuePair类型")
                                }, Charsets.UTF_8)
                            )
                        }
                    }

                    else -> {
                        requestBuilder.setEntity(data.toJsonString(), ContentType.APPLICATION_JSON)
                    }
                }
            }
        }
        // 设置请求头
        mergeHeader.forEach { (k, v) -> requestBuilder.setHeader(BasicHeader(k, v)) }
        requestBuilder.build()
    }
}

/**
 * http请求
 * @param method 请求方法
 * @param data 请求数据
 * @param headers 请求头
 * @param proxies 代理池，会随机选择一个代理
 */
fun String.doRequestAsync(
    method: HttpMethod,
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    callback: FutureCallback<HttpResponse>? = null,
    handleContent: (ByteArray?) -> Unit = {}
): Future<HttpResponse> {
    try {
        val httpClient = getHttpAsyncClient(proxies?.random())
        // 构建请求
        val request = generateAsyncRequest(this, method, data, headers)

        // 参考SimpleAsyncEntityConsumer的实现
        val consumer = object : AbstractBinAsyncEntityConsumer<ByteArray>() {
            private val buffer = ByteArrayBuffer(1024)

            override fun streamStart(contentType: ContentType?) {
                logger.error("url:[$this] method:[$method] status: streaming start")
            }

            override fun capacityIncrement(): Int {
                return Integer.MAX_VALUE
            }

            override fun data(src: ByteBuffer?, endOfStream: Boolean) {
                if (src == null) return
                if (src.hasArray()) {
                    val remaining = src.remaining()
                    val off = src.arrayOffset()
                    val position = src.position()
                    // 复制数据以避免ByteBuffer复用问题，因为ByteBuffer只能get一次
                    val byteArray = ByteArray(remaining).apply { src.get(this) }
                    buffer.append(src.array(), off + position, remaining)
                    handleContent(byteArray.copyOf())
                } else {
                    while (src.hasRemaining()) {
                        val remaining = src.remaining()
                        val byteArray = ByteArray(remaining).apply { src.get(this) }
                        handleContent(byteArray.copyOf())
                        buffer.append(byteArray, 0, byteArray.size)
                    }
                }
            }

            override fun generateContent(): ByteArray {
                return buffer.toByteArray()
            }

            override fun releaseResources() {
                buffer.clear()
            }
        }

        val responseConsumer =
            object : AbstractAsyncResponseConsumer<HttpResponse, ByteArray>(consumer) {
                override fun informationResponse(
                    response: org.apache.hc.core5.http.HttpResponse?,
                    context: HttpContext?
                ) {
                }

                override fun buildResult(
                    response: org.apache.hc.core5.http.HttpResponse?,
                    content: ByteArray?,
                    contentType: ContentType?
                ): HttpResponse {
                    logger.error("url:[$this] method:[$method] status: streaming end")
                    return HttpResponse(
                        body = content,
                        contentType = contentType.toString(),
                        contentLength = content?.size?.toLong(),
                        contentEncoding = contentType?.charset?.name(),
                        isStreaming = true,
                        headers = response?.headers?.associate { it.name to it.value },
                        code = response?.code,
                        version = response?.version.toString(),
                        reasonPhrase = response?.reasonPhrase,
                    )
                }
            }

        // 处理请求结果
        return httpClient.execute(request, responseConsumer, callback)
    } catch (e: Exception) {
        logger.error("url:[$this] method:[$method] error:[${e.message}]")
        e.printStackTrace()
        throw e
    }
}

fun String.doGetAsync(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    callback: FutureCallback<HttpResponse>? = null,
    handleContent: (ByteArray?) -> Unit = {}
) = doRequestAsync(HttpMethod.GET, data, headers, proxies, callback, handleContent)

fun String.doPostAsync(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    callback: FutureCallback<HttpResponse>? = null,
    handleContent: (ByteArray?) -> Unit = {}
) = doRequestAsync(HttpMethod.POST, data, headers, proxies, callback, handleContent)

fun String.doPutAsync(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    callback: FutureCallback<HttpResponse>? = null,
    handleContent: (ByteArray?) -> Unit = {}
) = doRequestAsync(HttpMethod.PUT, data, headers, proxies, callback, handleContent)

fun String.doDeleteAsync(
    data: Any? = null,
    headers: Map<String, String> = mapOf(),
    proxies: List<HttpHost>? = null,
    callback: FutureCallback<HttpResponse>? = null,
    handleContent: (ByteArray?) -> Unit = {}
) =
    doRequestAsync(HttpMethod.DELETE, data, headers, proxies, callback, handleContent)
package com.lumispring.framework.base.extension

import org.apache.hc.core5.http.HttpHost
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI


private val logger = logger()

/**
 * 发送WebSocket请求
 * @param waitConnect 是否等待连接成功，默认为true
 * @param proxies 代理列表，默认为null
 * @param handleMessage 消息处理函数
 */
fun String.doWsRequest(waitConnect: Boolean = true, proxies: List<HttpHost>? = null, handleMessage: (str: String?) -> Unit): WebSocketClient {
    val uri = URI(this)
    val ws = object: WebSocketClient(uri) {
        override fun onOpen(p0: ServerHandshake?) {
            logger.info("""
                【${this}】 websocket连接建立成功
                status: ${p0?.httpStatus}
                content: ${p0?.content?.toString(Charsets.UTF_8)}
            """.trimIndent())
        }

        override fun onMessage(p0: String?) {
            logger.info("【${this}】接受到数据：${p0}")
            handleMessage(p0)
        }

        override fun onClose(p0: Int, p1: String?, p2: Boolean) {
            logger.info("【${this}】 websocket连接关闭")
        }

        override fun onError(p0: Exception?) {
            logger.info("【${this}】 websocket请求出现异常")
            p0?.printStackTrace()
        }
    }

    return ws.also {
        // 配置代理
        if (proxies.isNotNullOrEmpty()) it.setProxy(proxies.random().let { host -> Proxy(Proxy.Type.HTTP, InetSocketAddress(host.hostName, host.port)) })
        if (waitConnect) it.connectBlocking() else it.connect()
    }
}
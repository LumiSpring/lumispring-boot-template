package com.lumispring.framework.web.service

import com.lumispring.framework.base.extension.checkNotNullOrEmpty
import com.lumispring.framework.base.extension.logger
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.web.extension.currentTraceId
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@Service
class SseEmitterService {

    private val sseCache: ConcurrentHashMap<String, SseEmitter> = ConcurrentHashMap()

    private val logger = logger()

    /**
     * 创建sse长链接
     * @param clientId 客户端id
     * @param onErrorCallback 错误回调
     * @param onTimeoutCallback 超时回调
     * @param onCompletionCallback 完成回调
     */
    fun createSseEmitter(
        clientId: String? = currentTraceId(),
        onErrorCallback: Consumer<Throwable>? = null,
        onTimeoutCallback: Runnable? = null,
        onCompletionCallback: Runnable? = null
    ): SseEmitter {

        clientId.checkNotNullOrEmpty(
            errorCode = ErrorCode.SERVICE_ERROR,
            log = "SseEmitterService[createSseEmitter]：创建长链接失败，原因：clientId为空"
        )

        // 0表示不设置超时时间，默认30秒超时，超时时间未完成后会抛出超时异常：AsyncRequestTimeoutException
        val sseEmitter = SseEmitter(0L)
        // 添加回调事件
        onErrorCallback?.let { sseEmitter.onError(it) }
        onTimeoutCallback?.let { sseEmitter.onTimeout(it) }
        onCompletionCallback?.let { sseEmitter.onCompletion(it) }
        // 存储到缓存中
        sseCache[clientId] = sseEmitter

        logger.info("创建sse长链接，clientId: {${clientId}}")

        return sseEmitter
    }

    /**
     * 获取sse长链接
     * @param clientId 客户端id
     */
    fun getSseEmitter(clientId: String? = currentTraceId()): SseEmitter? {
        if (clientId == null) return null
        return sseCache[clientId]
    }

    /**
     * 发送消息给指定客户端
     * @param data 数据
     * @param clientId 客户端id，默认当前
     * @param retry 是否失败重试，默认true
     * @param maxRetryCount 最大重试次数，默认10次
     * @param mediaType 数据类型，默认text/event-stream
     */
    fun <T : Any> send(
        data: T,
        clientId: String? = currentTraceId(),
        retry: Boolean = true,
        maxRetryCount: Int = 10,
    ) {
        clientId.checkNotNullOrEmpty(
            errorCode = ErrorCode.SERVICE_ERROR,
            log = "客户端：{${clientId}}推送消息失败，原因：传入的客户端id为空"
        )
        val sse = getSseEmitter(clientId)
        sse.checkNotNullOrEmpty(
            errorCode = ErrorCode.SERVICE_ERROR,
            log = "客户端：{${clientId}}推送消息失败，原因：长链接未创建，失败消息：${data}"
        )

        val sseEventBuilder = SseEmitter.event().id(clientId).data(data)
        try {
            sse.send(sseEventBuilder)
            logger.info("客户端：{${clientId}}推送消息成功，消息：${data}")
        } catch (e: Exception) {
            if (!retry) return
            for (i in 1..maxRetryCount){
                try {
                    sse.send(sseEventBuilder)
                    logger.info("客户端：{${clientId}}推送消息成功，消息：${data}")
                    break
                } catch (e: Exception) {
                    logger.info("客户端：{${clientId}}推送消息失败，原因：${e.message}，重试第${i}次")
                }
            }
        }
    }

    /**
     * 发送消息给全部客户端
     * @param data 数据
     */
    fun <T : Any> sendAll(data: T) {
        sseCache.keys().asIterator().forEach {
            send(data, it)
        }
    }

    /**
     * 关闭sse长链接
     */
    fun close(clientId: String? = currentTraceId()) {
        sseCache[clientId]?.complete()
        logger.info("关闭sse长链接，clientId: {${clientId}}")
    }

    /**
     * 移除某客户端
     */
    fun remove(clientId: String? = currentTraceId()) {
        sseCache[clientId]?.complete()
        sseCache.remove(clientId)
        logger.info("移除sse长链接，clientId: {${clientId}}")
    }

    /**
     * 清空客户端缓存
     */
    fun clean() {
        sseCache.keys().asIterator().forEach {
            remove(it)
        }
        sseCache.clear()
        logger.info("清空SseEmitterService客户端缓存")
    }
}
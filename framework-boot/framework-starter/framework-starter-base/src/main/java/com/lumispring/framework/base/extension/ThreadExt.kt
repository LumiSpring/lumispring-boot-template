package com.lumispring.framework.base.extension

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor

/**
 * 默认全局线程池
 */
val DEFAULT_THREAD_POOL by lazy {
    getBeanOrNull("defaultThreadPool", ThreadPoolExecutor::class.java) ?: createThreadPool()
}

/**
 * 创建线程池
 * @param corePoolSize 核心线程数
 * @param maxPoolSize 最大线程数，为了性能最佳，最好与核心线程数保持一致
 * @param keepAliveTime 线程空闲时间
 * @param unit 时间单位
 * @param workQueue 工作队列
 * @param threadFactory 线程工厂
 * @param handler 拒绝策略，当工作队列满且不存在可用的线程时，执行拒绝策略
 *     常见策略：
 *     1. AbortPolicy：直接丢弃任务，抛出异常，这是默认的策略。
 *     2. CallerRunsPolicy：直接在调用者线程中执行任务，如果线程池已满，则直接在调用者线程中执行任务。
 *     3. DiscardOldestPolicy：丢弃队列中最旧的任务，然后重新尝试执行任务。
 *     4. DiscardPolicy：直接丢弃任务，不抛出异常。
 * @param allowCoreThreadTimeOut 是否允许核心线程超时，如果设置为true，则核心线程会在空闲时被销毁
 */
fun createThreadPool(
    corePoolSize: Int = Runtime.getRuntime().availableProcessors(),
    maxPoolSize: Int = corePoolSize,
    keepAliveTime: Long = 60,
    unit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.SECONDS,
    workQueue: java.util.concurrent.BlockingQueue<Runnable> = LinkedBlockingQueue(corePoolSize * 2),
    threadFactory: java.util.concurrent.ThreadFactory = Executors.defaultThreadFactory(),
    handler: java.util.concurrent.RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy(),
    allowCoreThreadTimeOut: Boolean = false
):ThreadPoolExecutor{
    return ThreadPoolExecutor(
        corePoolSize,
        maxPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        handler
    ).apply {
        // 是否允许销毁空闲核心线程
        this.allowCoreThreadTimeOut(allowCoreThreadTimeOut)
    }
}
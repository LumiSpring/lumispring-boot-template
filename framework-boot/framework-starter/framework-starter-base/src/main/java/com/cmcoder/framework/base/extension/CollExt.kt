package com.lumispring.framework.base.extension

import java.util.concurrent.locks.ReentrantLock

/**
 * 过滤掉所有为null或空的元素
 */
fun <T> Collection<T>.filterNotNullOrEmpty():List<T> {
    return this.filter { it.isNotNullOrEmpty() }
}

/**
 * 固定容量的list，超出容量时会移除旧元素
 * @param capacity 容量
 * @param threadSafe 是否线程安全
 */
class FixedCapacityList<T>(private val capacity: Int, threadSafe: Boolean = false) {
    private val deque = ArrayDeque<T>(capacity)
    private val lock = if (threadSafe) ReentrantLock() else null

    fun add(element: T) {
        lock?.lock()
        if (deque.size == capacity) {
            // 如果达到容量上限，移除最早的元素（队列头部）
            deque.removeFirst()
        }
        // 添加新元素到队列尾部
        deque.addLast(element)
        lock?.unlock()
    }

    fun get(index: Int): T? {
        lock?.lock()
        return deque.getOrNull(index).also {
            lock?.unlock()
        }
    }

    fun size(): Int {
        lock?.lock()
        return deque.size.also {
            lock?.unlock()
        }
    }

    override fun toString(): String {
        lock?.lock()
        return deque.toString().also {
            lock?.unlock()
        }
    }
}


/**
 * 创建List
 */
fun <T> createList(len: Int, func: () -> T): List<T> {
    return mutableListOf<T>().apply {
        for (i in 0 until len) {
            add(func())
        }
    }
}
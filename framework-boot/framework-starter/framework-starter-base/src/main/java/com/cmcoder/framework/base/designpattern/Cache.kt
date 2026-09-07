package com.lumispring.framework.base.designpattern

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit


/**
 * 默认缓存
 * @param initCapacity 初始化容量
 * @param maxCapacity 最大容量
 * @param expireAfterWrite 写入后过期时间
 * @param expireAfterAccess 访问后过期时间
 * @param timeUnit 时间单位
 */
class DefaultCache<K:Any, V:Any>(
    initCapacity: Int? = 16,
    maxCapacity: Long? = 128,
    expireAfterWrite: Long? = null,
    expireAfterAccess: Long? = null,
    timeUnit: TimeUnit = TimeUnit.MINUTES
) {
    private val cache:Cache<K,V> = Caffeine.newBuilder().let { builder->
        initCapacity?.let { builder.initialCapacity(it) }
        maxCapacity?.let { builder.maximumSize(it) }
        expireAfterWrite?.let { builder.expireAfterWrite(it, timeUnit) }
        expireAfterAccess?.let { builder.expireAfterAccess(it, timeUnit) }
        builder.build()
    }

    fun getOrPut(key: K, block: () -> V): V {
        var value = cache.getIfPresent(key)
        if (value == null) {
            val newValue = block()
            put(key, newValue)
            value = newValue
        }
        return value
    }

    fun getOrPut(key: K, newValue: V): V {
        return getOrPut(key) { newValue }
    }

    fun put(key: K, value: V) {
        cache.put(key, value)
    }

    operator fun get(key: K): V? {
        return cache.getIfPresent(key)
    }

    operator fun set(key: K, value: V) = put(key, value)

    operator fun contains(key: K) = cache.getIfPresent(key) != null

    fun getAllAsMap() = cache.asMap()

    fun clear() {
        cache.cleanUp()
    }
}
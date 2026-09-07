package com.lumispring.framework.database.redis.core

import com.lumispring.framework.base.extension.*
import org.redisson.api.RBloomFilter
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResults
import org.springframework.data.geo.Metrics
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.scripting.ScriptSource
import java.util.concurrent.TimeUnit


val DEFAULT_REDIS_TEMPLATE: RedisTemplate<String, Any> by lazy {
    getBean("IRedisTemplate", RedisTemplate::class.java) as RedisTemplate<String, Any>
}
val DEFAULT_REDISSON_CLIENT: RedissonClient by lazy {
    getBean(RedissonClient::class.java)
}

class RedisClient() {
    companion object {
        /***************************************通用操作*****************************************/

        /**
         * 删除key
         * @param   key 键
         */
        fun del(key: String) {
            DEFAULT_REDIS_TEMPLATE.delete(key)
        }

        /**
         * 设置过期时间
         * @param   key 键
         * @param   timeout 过期时间
         * @param   timeUnit 时间单位
         */
        fun expire(key: String, timeout: Long, timeUnit: TimeUnit = TimeUnit.SECONDS) {
            DEFAULT_REDIS_TEMPLATE.expire(key, timeout, timeUnit)
        }

        /**
         * 重命名key
         * @param   oldKey 旧键
         * @param   newKey 新键
         */
        fun rename(oldKey: String, newKey: String) {
            DEFAULT_REDIS_TEMPLATE.rename(oldKey, newKey)
        }

        /**
         * 持久化key，移除过期时间
         * @param   key 键
         */
        fun persist(key: String) {
            DEFAULT_REDIS_TEMPLATE.persist(key)
        }

        /**
         * 获取过期时间
         * @param   key 键
         * @param   timeUnit 时间单位，默认秒
         */
        fun ttl(key: String, timeUnit: TimeUnit = TimeUnit.SECONDS): Long? {
            return DEFAULT_REDIS_TEMPLATE.getExpire(key, timeUnit)
        }

        /**
         * 判断key是否存在
         * @param   key 键
         */
        fun exists(key: String): Boolean {
            return DEFAULT_REDIS_TEMPLATE.hasKey(key).isTrue()
        }

        /**
         * 获取布隆过滤器
         * @param   key 键
         * @param   expectedInsertions 预期插入数量
         * @param   falseProbability 误差率
         */
        fun <T> getBloomFilter(
            key: String,
            expectedInsertions: Long = 10000000,
            falseProbability: Double = 0.01
        ): RBloomFilter<T> {
            return DEFAULT_REDISSON_CLIENT.getBloomFilter<T>(key).apply {
                this.tryInit(expectedInsertions, falseProbability)
            }
        }

        /**
         * 执行lua脚本
         * @param   redisScript 脚本
         * @param   keys 键集合
         * @param   args 参数
         * @param   scriptScore 脚本源
         */
        inline fun <reified T> execute(
            redisScript: String,
            keys: List<String>,
            vararg args: Any,
            scriptScore: ScriptSource? = null
        ): T? {
            return DEFAULT_REDIS_TEMPLATE.execute(DefaultRedisScript(redisScript, T::class.java).apply {
                scriptScore?.let { this.setScriptSource(it) }
            }, keys, *args)
        }

        /**
         * 分布式锁
         * @param   key 键
         * @param   callback 回调
         */
        inline fun lock(key: String, callback: (lock: RLock) -> Any?): Any? {
            val r = DEFAULT_REDISSON_CLIENT.getLock(key)
            r.lock()
            try {
                return callback(r)
            } finally {
                r.unlock()
            }
        }

        /**
         * 尝试获取分布式锁
         * @param   key 键
         * @param   maxWaitTime 最大等待时间
         * @param   leaseTime 锁的有效时间，到期自动释放
         * @param   timeUnit 时间单位
         */
        inline fun tryLock(
            key: String,
            maxWaitTime: Long = 10,
            leaseTime: Long = 10,
            timeUnit: TimeUnit = TimeUnit.SECONDS,
            callback: (lock: RLock) -> Any?
        ): Any? {
            val r = DEFAULT_REDISSON_CLIENT.getLock(key)
            if (r.tryLock(maxWaitTime, leaseTime, timeUnit)) {
                try {
                    return callback(r)
                } finally {
                    r.unlock()
                }
            }
            return null
        }

        /**
         * 获取分布式锁
         * @param   key 键
         */
        fun getLock(key: String): RLock {
            return DEFAULT_REDISSON_CLIENT.getLock(key)
        }


        /***************************************string操作*****************************************/

        /**
         * 获取string结构的值，并转换成相应类型
         * @param   key 键
         */
        inline fun <reified T> get(key: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().get(key)?.toObject<T>()
        }

        /**
         * 批量获取string结构的值，并转换成相应类型
         * @param   keys 键集合
         */
        inline fun <reified T> mGet(keys: Collection<String>): List<T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().multiGet(keys)?.toObject<List<T>>()
        }

        /**
         * 设置string结构的值
         * @param   key 键
         * @param   value 值
         * @param   timeout 过期时间，默认-1为不过期
         * @param   timeUnit 时间单位，默认秒
         */
        fun set(key: String, value: Any, timeout: Long = -1L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
            DEFAULT_REDIS_TEMPLATE.opsForValue().set(key, value, timeout, timeUnit)
        }

        /**
         * 批量设置string结构的值
         * @param   keyValues 键值对
         */
        fun mSet(keyValues: Map<String, Any>) {
            DEFAULT_REDIS_TEMPLATE.opsForValue().multiSet(keyValues)
        }

        /**
         * 设置string结构的值，如果key不存在
         * @param   key 键
         * @param   value 值
         * @param   timeout 过期时间，默认-1为不过期
         * @param   timeUnit 时间单位，默认秒
         */
        fun setNx(key: String, value: Any, timeout: Long = -1L, timeUnit: TimeUnit = TimeUnit.SECONDS): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().setIfAbsent(key, value, timeout, timeUnit)!!
        }

        /**
         * 批量设置string结构的值，如果key不存在
         * @param   keyValues 键值对
         */
        fun mSetNx(keyValues: Map<String, Any>): Boolean? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().multiSetIfAbsent(keyValues)
        }

        /**
         * 覆盖从指定位置开始的值
         * @param   key 键
         * @param   value 值
         * @param   offset 位置
         */
        fun setRange(key: String, value: Any, offset: Long) {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().set(key, value, offset)
        }

        /**
         * 在原有的值基础上新增字符串到末尾
         * @param   key 键
         * @param   value 值
         */
        fun append(key: String, value: Any): Int? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().append(key, value.toJsonString().parseJson<String>() ?: "")
        }

        /**
         * 自增
         * @param   key 键
         * @param   delta 增量，默认1
         */
        fun incr(key: String, delta: Long = 1): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().increment(key, delta)
        }

        /**
         * 自减
         * @param   key 键
         * @param   delta 减量，默认1
         */
        fun decr(key: String, delta: Long = 1): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForValue().decrement(key, delta)
        }

        /***************************************list操作*****************************************/

        /**
         * 往list结构的左边插入值
         * @param   key 键
         * @param   values 值
         */
        fun lPush(key: String, vararg values: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().leftPushAll(key, values)
        }

        /**
         * 往list结构的右边插入值
         * @param   key 键
         * @param   values 值
         */
        fun rPush(key: String, vararg values: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().rightPushAll(key, values)
        }

        /**
         * 从list结构的左边获取指定范围的值
         * @param   key 键
         * @param   start 开始位置
         * @param   end 结束位置
         */
        inline fun <reified T> lRange(key: String, start: Long, end: Long): List<T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().range(key, start, end)?.toObject<List<T>>()
        }

        /**
         * 从list结构获取指定索引位置值
         * @param   key 键
         * @param   index 索引
         */
        inline fun <reified T> lIndex(key: String, index: Long): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().index(key, index)?.toObject<T>()
        }

        /**
         * 获取指定键的list值的长度
         */
        fun lLen(key: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().size(key)
        }

        /**
         * 从list结构的左边弹出值
         * @param   key 键
         * @return  T?
         */
        inline fun <reified T> lPop(key: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().leftPop(key)?.toObject<T>()
        }

        /**
         * 从list结构的右边弹出值
         * @param   key 键
         * @return  T?
         */
        inline fun <reified T> rPop(key: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().rightPop(key)?.toObject<T>()
        }

        /**
         * 根据参数 count 的值，移除列表中与参数 value 相等的元素
         * @param key 键
         * @param count count > 0，移除等于从头到尾移除count个value的元素
         *              count < 0，移除等于从尾到头移除count个value的元素
         *              count = 0，移除等于value所有的元素
         * @param value 值
         * @return 返回移除元素的个数
         */
        fun lRem(key: String, count: Long, value: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForList().remove(key, count, value)
        }

        /**
         * 保留指定区间内的元素，其他元素全部删除
         * @param key 键
         * @param start 开始
         * @param end 结束
         */
        fun lTrim(key: String, start: Long, end: Long) {
            DEFAULT_REDIS_TEMPLATE.opsForList().trim(key, start, end)
        }

        /**
         * 将列表key下标为index的元素设置为value
         * @param key 键
         * @param index 下标
         * @param value 值
         */
        fun lSet(key: String, index: Long, value: Any) {
            DEFAULT_REDIS_TEMPLATE.opsForList().set(key, index, value)
        }

        /**
         * 往列表key的pivot元素的前面或者后面插入元素value
         * @param key 键
         * @param pivot 基准元素
         * @param value 值
         * @param isLeft true：左侧插入，false：右侧插入
         * @return 返回插入后列表的长度
         */
        fun lInsert(key: String, where: String, pivot: Any, value: Any, isLeft: Boolean = false): Long? {
            return if (isLeft) DEFAULT_REDIS_TEMPLATE.opsForList().leftPush(key, pivot, value)
            else DEFAULT_REDIS_TEMPLATE.opsForList().rightPush(key, pivot, value)
        }

        /***************************************hash操作*****************************************/

        /**
         * 获取hash结构的值，并转换成相应类型
         * @param   key 键
         * @param   field 字段
         */
        inline fun <reified T> hGet(key: String, field: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().get(key, field)?.toObject<T>()
        }

        /**
         * 批量获取hash结构的值，并转换成相应类型
         * @param   key 键
         * @param   fields 字段集合
         */
        inline fun <reified T> hMGet(key: String, fields: Set<String>): List<T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().multiGet(key, fields).toObject<List<T>>()
        }

        /**
         * 获取hash结构的所有值，并转换成相应类型
         * @param   key 键
         */
        inline fun <reified T> hMGetAll(key: String): Map<String, T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().entries(key).toObject<Map<String, T>>()
        }

        /**
         * 设置hash结构的值
         * @param   key 键
         * @param   field 字段
         * @param   value 值
         */
        fun hSet(key: String, field: String, value: Any) {
            DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().put(key, field, value)
        }

        /**
         * 设置hash结构的值，如果字段不存在
         * @param   key 键
         * @param   field 字段
         * @param   value 值
         */
        fun hSetNx(key: String, field: String, value: Any): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().putIfAbsent(key, field, value)
        }

        /**
         * 批量设置hash结构的值
         * @param   key 键
         * @param   map 字段值集合
         */
        fun hMSet(key: String, map: Map<String, Any>) {
            DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().putAll(key, map)
        }

        /**
         * 获取hash结构的长度
         * @param   key 键
         */
        fun hLen(key: String): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().size(key)
        }

        /**
         * 获取hash结构的所有字段
         * @param   key 键
         */
        fun hKeys(key: String): Set<String> {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().keys(key)
        }

        /**
         * 获取hash结构的所有值
         * @param   key 键
         */
        inline fun <reified T> hVals(key: String): List<T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().values(key).toObject<List<T>>()
        }

        /**
         * 判断hash结构的字段是否存在
         * @param   key 键
         * @param   field 字段
         */
        fun hExists(key: String, field: String): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().hasKey(key, field)
        }

        /**
         * 删除hash结构的字段
         * @param   key 键
         * @param   fields 字段
         */
        fun hDel(key: String, vararg fields: String): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().delete(key, *fields)
        }

        /**
         * 自增
         * @param   key 键
         * @param   field 字段
         * @param   delta 增量，默认1
         */
        fun hIncr(key: String, field: String, delta: Long = 1): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().increment(key, field, delta)
        }

        /**
         * 自减
         * @param   key 键
         * @param   field 字段
         * @param   delta 减量，默认1
         */
        fun hDecr(key: String, field: String, delta: Long = 1): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHash<String, Any>().increment(key, field, -delta)
        }

        /***************************************set操作*****************************************/

        /**
         * 添加元素到集合
         * @param   key 键
         * @param   values 值
         */
        fun sAdd(key: String, vararg values: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().add(key, *values)
        }

        /**
         * 获取集合的元素数量
         * @param   key 键
         */
        fun sCard(key: String): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().size(key) ?: 0
        }

        /**
         * 获取集合的迭代器
         * @param   key 键
         * @param   pattern 匹配模式
         * @param   count 数量
         */
        fun <T> sScan(key: String, pattern: String? = null, count: Long? = null): Cursor<Any> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().scan(key, ScanOptions.scanOptions().let { builder ->
                pattern?.let { builder.match(it) }
                count?.let { builder.count(it) }
                builder.build()
            })
        }


        /**
         * 获取集合的所有元素
         * @param   key 键
         */
        inline fun <reified T> sMembers(key: String): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().members(key)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 判断元素是否是集合的成员
         * @param   key 键
         * @param   value 值
         */
        fun sIsMember(key: String, value: Any): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().isMember(key, value).isTrue()
        }

        /**
         * 移除集合中的元素
         * @param   key 键
         * @param   values 值
         */
        fun sRem(key: String, vararg values: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().remove(key, *values)
        }

        /**
         * 随机移除并返回集合中的元素
         * @param   key 键
         */
        inline fun <reified T> sPop(key: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().pop(key)?.toObject<T>()
        }

        /**
         * 随机移除并返回集合中的元素
         * @param   key 键
         */
        inline fun <reified T> sPop(key: String, count: Long): List<T>? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().pop(key, count)?.toObject<List<T>>()
        }

        /**
         * 将某个元素从一个集合移动到另一个集合
         * @param   srcKey 源键
         * @param   destKey 目标键
         * @param   value 值
         */
        fun sMove(srcKey: String, destKey: String, value: Any): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().move(srcKey, value, destKey).isTrue()
        }

        /**
         * 获取集合中多个随机数
         * @param   key 键
         * @param   count 数量
         */
        inline fun <reified T> sRandMember(key: String, count: Long): List<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().randomMembers(key, count)?.toObject<List<T>>() ?: emptyList()
        }

        /**
         * 获取集合中一个随机数
         * @param   key 键
         */
        inline fun <reified T> sRandMember(key: String): T? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().randomMember(key).toObject<T>()
        }

        /**
         * 获取多个集合的交集
         * @param   keys 键集合
         */
        inline fun <reified T> sInter(keys: Collection<String>): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().intersect(keys)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取多个集合的交集，并存储到目标集合
         * @param   keys 键集合
         * @param   destKey 目标键
         */
        fun sInterStore(keys: Collection<String>, destKey: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().intersectAndStore(keys, destKey)
        }

        /**
         * 获取多个集合的并集
         * @param   keys 键集合
         */
        inline fun <reified T> sUnion(keys: Collection<String>): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().union(keys)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取多个集合的并集，并存储到目标集合
         * @param   keys 键集合
         * @param   destKey 目标键
         */
        fun sUnionStore(keys: Collection<String>, destKey: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().unionAndStore(keys, destKey)
        }

        /**
         * 获取多个集合的差集
         * @param   keys 键集合
         */
        inline fun <reified T> sDiff(keys: Collection<String>): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().difference(keys)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取多个集合的差集，并存储到目标集合
         * @param   keys 键集合
         * @param   destKey 目标键
         */
        fun sDiffStore(keys: Collection<String>, destKey: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForSet().differenceAndStore(keys, destKey)
        }

        /***************************************zset操作*****************************************/

        /**
         * 添加元素到有序集合
         * @param   key 键
         * @param   value 值
         * @param   score 分数
         */
        fun zAdd(key: String, value: Any, score: Double): Boolean {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().add(key, value, score).isTrue()
        }

        /**
         * 批量添加元素到有序集合
         * @param   key 键
         * @param   values 值
         */
        fun zAdd(key: String, values: Map<Any, Double>) {
            DEFAULT_REDIS_TEMPLATE.opsForZSet().add(key, values.map { (k, v) -> DefaultTypedTuple(k, v) }.toSet())
        }

        /**
         * 获取有序集合的元素数量
         * @param   key 键
         */
        fun zCard(key: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().zCard(key)
        }

        /**
         * 获取有序集合中指定分数区间的元素数量
         * @param   key 键
         * @param   min 最小分数
         * @param   max 最大分数
         */
        fun zCount(key: String, min: Double, max: Double): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().count(key, min, max)
        }

        /**
         * 获取有序集合中某元素的分数
         */
        fun zScore(key: String, value: Any): Double? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().score(key, value)
        }

        /**
         * 获取有序集合中某元素的排名
         * @param   key 键
         * @param   value 值
         */
        fun zRank(key: String, value: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().rank(key, value)
        }

        /**
         * 获取有序集合中某元素的排名，按分数从大到小
         * @param   key 键
         * @param   value 值
         */
        fun zRevRank(key: String, value: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().reverseRank(key, value)
        }

        /**
         * 获取指定区间内的集合元素，按分数从小到大
         * @param   key 键
         * @param   start 开始位置
         * @param   end 结束位置
         */
        inline fun <reified T> zRange(key: String, start: Long, end: Long): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().range(key, start, end)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取指定区间内的集合元素，按分数从大到小
         * @param   key 键
         * @param   start 开始位置
         * @param   end 结束位置
         */
        inline fun <reified T> zRevRange(key: String, start: Long, end: Long): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().reverseRange(key, start, end)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取指定分数区间内的集合元素，按分数从小到大
         * @param   key 键
         * @param   min 最小分数
         * @param   max 最大分数
         */
        inline fun <reified T> zRangeByScore(key: String, min: Double, max: Double): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().rangeByScore(key, min, max)?.toObject<Set<T>>() ?: emptySet()
        }

        /**
         * 获取指定分数区间内的集合元素，按分数从大到小
         * @param   key 键
         * @param   min 最小分数
         * @param   max 最大分数
         */
        inline fun <reified T> zRevRangeByScore(key: String, min: Double, max: Double): Set<T> {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().reverseRangeByScore(key, min, max)?.toObject<Set<T>>()
                ?: emptySet()
        }

        /**
         * 增加成员的分数
         * @param   key 键
         * @param   value 值
         * @param   delta 增量
         */
        fun zIncrBy(key: String, value: Any, delta: Double): Double? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().incrementScore(key, value, delta)
        }

        /**
         * 将多个有序集合的并集存储到一个新的有序集合中
         * @param   key 键
         * @param   otherKeys 其他键
         * @param   destKey 目标键
         */
        fun zUnionStore(key: String, otherKeys: Collection<String>, destKey: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().unionAndStore(key, otherKeys, destKey)
        }

        /**
         * 将多个有序集合的交集存储到一个新的有序集合中
         * @param   key 键
         * @param   otherKeys 其他键
         * @param   destKey 目标键
         */
        fun zInterStore(key: String, otherKeys: Collection<String>, destKey: String): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().intersectAndStore(key, otherKeys, destKey)
        }

        /**
         * 获取有序集合的迭代器
         * @param   key 键
         * @param   pattern 匹配模式
         * @param   count 数量
         */
        fun zScan(key: String, pattern: String? = null, count: Long? = null): Cursor<TypedTuple<Any>> {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().scan(key, ScanOptions.scanOptions().let { builder ->
                pattern?.let { builder.match(it) }
                count?.let { builder.count(it) }
                builder.build()
            })
        }

        /**
         * 删除有序集合中的元素
         * @param   key 键
         * @param   values 值
         */
        fun zRem(key: String, vararg values: Any): Long? {
            return DEFAULT_REDIS_TEMPLATE.opsForZSet().remove(key, *values)
        }

        /****************************************HyperLogLog操作*****************************************/

        /**
         * 添加元素到HyperLogLog
         * @param   key 键
         * @param   values 值
         */
        fun pfAdd(key: String, vararg values: Any): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHyperLogLog().add(key, *values)
        }

        /**
         * 获取HyperLogLog的基数
         * @param   key 键
         */
        fun pfCount(key: String): Long {
            return DEFAULT_REDIS_TEMPLATE.opsForHyperLogLog().size(key)
        }

        /**
         * 合并多个HyperLogLog
         * @param   destKey 目标键
         * @param   sourceKeys 源键
         */
        fun pfMerge(destKey: String, vararg sourceKeys: String) {
            DEFAULT_REDIS_TEMPLATE.opsForHyperLogLog().union(destKey, *sourceKeys)
        }

        /**
         * 删除HyperLogLog
         * @param   key 键
         * @return  Boolean
         */
        fun pfDel(key: String) {
            DEFAULT_REDIS_TEMPLATE.opsForHyperLogLog().delete(key)
        }

        /****************************************Geo操作*****************************************/

        /**
         * 添加地理位置信息
         * @param   key 键
         * @param   longitude 经度
         * @param   latitude 纬度
         * @param   member 成员
         */
        fun geoAdd(key: String, longitude: Double, latitude: Double, member: Any) {
            DEFAULT_REDIS_TEMPLATE.opsForGeo().add(key, org.springframework.data.geo.Point(longitude, latitude), member)
        }

        /**
         * 获取两个地理位置之间的距离
         * @param   key 键
         * @param   member1 成员1
         * @param   member2 成员2
         * @param   metric 单位，默认为米
         */
        fun geoDist(key: String, member1: Any, member2: Any, metric: Metrics = Metrics.MILES): Distance? {
            return DEFAULT_REDIS_TEMPLATE.opsForGeo().distance(key, member1, member2, Metrics.MILES)
        }

        /**
         * 获取地理位置的hash值
         * @param   key 键
         * @param   members 成员
         */
        fun geoHash(key: String, vararg members: Any): List<String>? {
            return DEFAULT_REDIS_TEMPLATE.opsForGeo().hash(key, *members)
        }

        /**
         * 获取地理位置的坐标
         * @param   key 键
         * @param   members 成员
         */
        fun geoPos(key: String, vararg members: Any): List<org.springframework.data.geo.Point>? {
            return DEFAULT_REDIS_TEMPLATE.opsForGeo().position(key, *members)
        }

        /**
         * 获取指定地理位置附近的成员
         * @param   key 键
         * @param   member 成员
         * @param   distance 距离
         * @param   metric 单位，默认为米
         */
        fun geoRadius(
            key: String,
            member: Any,
            distance: Double,
            metric: Metrics = Metrics.MILES
        ): GeoResults<GeoLocation<Any>>? {
            return DEFAULT_REDIS_TEMPLATE.opsForGeo().radius(key, member, Distance(distance, metric))
        }
    }
}
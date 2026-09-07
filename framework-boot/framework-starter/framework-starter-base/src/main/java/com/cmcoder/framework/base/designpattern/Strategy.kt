package com.lumispring.framework.base.designpattern

import com.lumispring.framework.base.extension.getBeans
import com.lumispring.framework.base.model.ErrorCode
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Lazy
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

/**
 * 抽象策略类
 */
abstract class IStrategy<REQUEST, RESPONSE> {
    /**
     * 策略类的标识
     */
    abstract fun mask(): String

    /**
     * 策略类的执行方法
     * @param param 策略类的参数
     */
    abstract fun execute(param: REQUEST): RESPONSE?
}


/**
 * 策略上下文，用于获取所有的策略类
 */
@Component
@Lazy(false)
class StrategyContext() : ApplicationRunner, Ordered {

    companion object {
        private var strategyContainer: MutableMap<String, IStrategy<*, *>> = mutableMapOf()

        private fun getRealMarkKey(strategy: IStrategy<*, *>, mark: String): String {
            return "${strategy.javaClass.name}#${mark}"
        }

        /**
         * 获取指定类的策略类
         * @param clazz 策略类的类型
         */
        fun <T : IStrategy<*, *>> get(clazz: Class<T>): List<T> {
            return strategyContainer.values.filter { strategy ->
                clazz.isAssignableFrom(strategy.javaClass)
            }.map { it as T }
        }

        /**
         * 获取指定类和标识的策略类
         * @param clazz 策略类的类型
         * @param mark 策略类的标识
         */
        fun <T : IStrategy<*, *>> get(clazz: Class<T>, mark: String): T? {
            return get(clazz).find { it.mask() == mark }
        }
    }

    override fun run(args: ApplicationArguments?) {
        // 从容器中获取所有的策略类并放入策略容器中
        val strategyMaps = getBeans(IStrategy::class.java)
        strategyMaps.forEach { (_, strategy) ->
            val realMark = getRealMarkKey(strategy, strategy.mask())
            if (strategyContainer.containsKey(realMark)) {
                throw ErrorCode.SERVICE_ERROR.exception(log="策略类[${realMark}]已经存在")
            }
            strategyContainer[realMark] = strategy
        }
    }

    override fun getOrder(): Int {
        return 1
    }
}
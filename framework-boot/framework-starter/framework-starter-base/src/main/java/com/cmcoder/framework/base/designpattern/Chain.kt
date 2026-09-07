package com.lumispring.framework.base.designpattern

import com.lumispring.framework.base.extension.getBeans
import com.lumispring.framework.base.extension.isNull
import com.lumispring.framework.base.model.ErrorCode
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

/**
 * 责任链抽象类
 */
abstract class IChain<REQUEST> : Ordered {
    /**
     * 责任链标识：同一条责任链的标识必须一致
     */
    abstract fun mark(): String

    /**
     * 责任链执行方法
     */
    abstract fun handler(requestParam: REQUEST)
}

@Component
class ChainerContext : ApplicationRunner {
    companion object {
        private val chainContainer: MutableMap<String, MutableList<IChain<*>>> = mutableMapOf()

        fun <T> handle(mark: String, requestParam: T, reversed:Boolean = false) {
            if (requestParam.isNull()) throw ErrorCode.REQUEST_PARAM_ERROR.exception(log="参数不能为空")
            (if (reversed)chainContainer[mark]?.reversed() else chainContainer[mark])?.forEach { chain->
                (chain as IChain<T>).handler(requestParam)
            }
        }
    }

    override fun run(args: ApplicationArguments?) {
        // 从容器中获取所有的策略类并放入策略容器中
        val chainMaps = getBeans(IChain::class.java)
        chainMaps.forEach { (_, chain) ->
            if (chainContainer.containsKey(chain.mark())) {
                chainContainer[chain.mark()]?.apply {
                    add(chain)
                    sortBy { it.order }
                }
            } else {
                chainContainer[chain.mark()] = mutableListOf(chain)
            }
        }
    }
}
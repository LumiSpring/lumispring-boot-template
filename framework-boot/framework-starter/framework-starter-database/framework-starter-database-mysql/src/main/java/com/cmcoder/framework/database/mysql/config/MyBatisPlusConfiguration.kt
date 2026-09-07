package com.lumispring.framework.database.mysql.config

import com.baomidou.mybatisplus.annotation.DbType
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import kotlin.math.log

@Configuration
@EnableTransactionManagement
class MyBatisPlusConfiguration {

    /**
     * mbp拦截器
     */
    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        return MybatisPlusInterceptor().apply {
            // 分页插件
            this.addInnerInterceptor(PaginationInnerInterceptor(DbType.MYSQL))
        }
    }

    @Bean
    @ConditionalOnMissingBean(MyBatisPlusMetaObjectHandler::class)
    fun metaObjectHandler(): MyBatisPlusMetaObjectHandler {
        return MyBatisPlusMetaObjectHandler()
    }
}
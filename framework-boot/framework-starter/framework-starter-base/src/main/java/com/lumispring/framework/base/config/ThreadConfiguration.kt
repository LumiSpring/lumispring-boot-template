package com.lumispring.framework.base.config

import com.lumispring.framework.base.extension.createThreadPool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class ThreadConfiguration {

    @Bean("defaultThreadPool")
    fun defaultThreadPool() :ThreadPoolExecutor{
        return createThreadPool()
    }
}
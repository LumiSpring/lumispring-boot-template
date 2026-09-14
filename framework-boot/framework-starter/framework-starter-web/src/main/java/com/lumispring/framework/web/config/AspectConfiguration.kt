package com.lumispring.framework.web.config

import com.lumispring.framework.web.handler.ControllerAspect
import com.lumispring.framework.web.handler.LogControllerAop
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
class AspectConfiguration {
    /**
     * 控制器切面，负责执行所有自定义的控制器Aop
     */
    @Bean
    fun controllerAspect(): ControllerAspect {
        return ControllerAspect()
    }

    /**
     * 日志控制器Aop
     */
    @Bean
    @ConditionalOnMissingBean
    fun logControllerAop(): LogControllerAop {
        return LogControllerAop()
    }
}
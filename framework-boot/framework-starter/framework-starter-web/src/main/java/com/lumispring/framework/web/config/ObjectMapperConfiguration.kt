package com.lumispring.framework.web.config

import com.lumispring.framework.base.extension.getInitObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper

@Configuration
class ObjectMapperConfiguration {

    @Bean
    @Primary    // 优先使用
    @ConditionalOnMissingBean
    fun jsonMapper(): JsonMapper {
        return getInitObjectMapper()
    }
}

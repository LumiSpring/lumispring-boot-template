package com.lumispring.framework.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumispring.framework.base.extension.getInitObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class ObjectMapperConfiguration {

    @Bean
    @Primary    // 优先使用
    @ConditionalOnMissingBean
    fun objectMapper(customizer: Jackson2ObjectMapperBuilderCustomizer, builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        return getInitObjectMapper()
//        customizer.customize(builder)
//        return getInitObjectMapper(builder.build())
    }
}
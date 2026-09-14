package com.lumispring.framework.database.redis

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [RedisAutoConfiguration::class])
class RedisAutoConfiguration {
}
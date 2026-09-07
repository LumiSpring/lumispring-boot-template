package com.lumispring.framework.base

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [BaseAutoConfiguration::class])
class BaseAutoConfiguration {
}
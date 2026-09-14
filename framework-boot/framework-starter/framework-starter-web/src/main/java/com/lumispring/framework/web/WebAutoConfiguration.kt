package com.lumispring.framework.web

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
// 扫描指定类所在的包以及子包
@ComponentScan(basePackageClasses = [WebAutoConfiguration::class])
class WebAutoConfiguration {
}
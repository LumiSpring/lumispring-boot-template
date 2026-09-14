package com.lumispring.framework.database.mysql

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [MysqlAutoConfiguration::class])
class MysqlAutoConfiguration {
}
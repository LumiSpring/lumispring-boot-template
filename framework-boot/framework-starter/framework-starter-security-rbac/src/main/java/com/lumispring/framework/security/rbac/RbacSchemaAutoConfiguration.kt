package com.lumispring.framework.security.rbac

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * 在扫描 RBAC Mapper / Service 之前检测并补齐数据表。
 */
@Configuration
@EnableConfigurationProperties(RbacProperties::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
@AutoConfigureBefore(SecurityRbacAutoConfiguration::class)
@ConditionalOnBean(DataSource::class)
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true", matchIfMissing = true)
class RbacSchemaAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = ["security.rbac.schema-init.enabled"], havingValue = "true", matchIfMissing = true)
    fun rbacSchemaDatabaseInitializer(
        dataSource: DataSource,
        properties: RbacProperties
    ): RbacSchemaDatabaseInitializer {
        return RbacSchemaDatabaseInitializer(dataSource, properties)
    }
}

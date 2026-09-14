package com.lumispring.framework.security.rbac

import com.lumispring.framework.security.SecurityAutoConfiguration
import com.lumispring.framework.security.auth.AuthenticationResolver
import com.lumispring.framework.security.config.MybatisPlusCreateByMetaObject
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(
    basePackages = [
        "com.lumispring.framework.security.controller",
        "com.lumispring.framework.security.service",
        "com.lumispring.framework.security.mapper"
    ]
)
@MapperScan("com.lumispring.framework.security.mapper")
@AutoConfigureAfter(SecurityAutoConfiguration::class)
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true", matchIfMissing = true)
class SecurityRbacAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationResolver::class)
    fun authenticationResolver(): AuthenticationResolver {
        return TokenAuthenticationResolver()
    }

    @Bean
    @ConditionalOnMissingBean(MybatisPlusCreateByMetaObject::class)
    fun mybatisPlusCreateByMetaObject(): MybatisPlusCreateByMetaObject {
        return MybatisPlusCreateByMetaObject()
    }
}

package com.lumispring.framework.security

import com.lumispring.framework.security.auth.AuthenticationResolver
import com.lumispring.framework.security.config.SecurityInterceptor
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.config.aop.SecurityAspect
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(SecurityProperties::class)
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true", matchIfMissing = true)
class SecurityAutoConfiguration(
    private val resolvers: ObjectProvider<AuthenticationResolver>
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SecurityInterceptor(resolvers))
            .excludePathPatterns(DEFAULT_EXCLUDE_PATH_PATTERNS)
    }

    @Bean
    fun securityAspect(properties: SecurityProperties): SecurityAspect {
        return SecurityAspect(properties)
    }

    val DEFAULT_EXCLUDE_PATH_PATTERNS by lazy {
        listOf(
            "html",
            "js",
            "css",
            "jpg",
            "jpeg",
            "png",
            "gif",
            "svg",
            "bmp",
            "webp",
            "woff2",
            "ttf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "ppt",
            "pptx",
            "txt",
            "csv",
            "zip"
        ).map { "/**/*.$it" }
    }
}

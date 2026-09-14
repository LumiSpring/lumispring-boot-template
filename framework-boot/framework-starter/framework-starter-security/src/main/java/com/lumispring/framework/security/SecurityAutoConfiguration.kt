package com.lumispring.framework.security

import com.lumispring.framework.security.config.SecurityInterceptor
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
// 扫描指定类所在的包以及子包
@ComponentScan(basePackageClasses = [SecurityAutoConfiguration::class])
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true", matchIfMissing = true)
class SecurityAutoConfiguration : WebMvcConfigurer{

    @Autowired
    private lateinit var properties: SecurityProperties

    @Autowired
    @Qualifier("securityRedisTemplate")
    private lateinit var redisTemplate: RedisTemplate<String, String>

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SecurityInterceptor(properties, redisTemplate))
            .excludePathPatterns(DEFAULT_EXCLUDE_PATH_PATTERNS)
    }

    /**
     * 默认排除的静态文件类型
     */
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
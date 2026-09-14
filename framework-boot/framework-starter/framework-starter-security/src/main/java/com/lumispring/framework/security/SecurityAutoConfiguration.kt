package com.lumispring.framework.security

import com.lumispring.framework.security.config.SecurityInterceptor
import com.lumispring.framework.security.config.SecurityProperties
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@ComponentScan(basePackageClasses = [SecurityAutoConfiguration::class])
@MapperScan("com.lumispring.framework.security.mapper")
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true", matchIfMissing = true)
class SecurityAutoConfiguration : WebMvcConfigurer{

    @Autowired
    private lateinit var properties: SecurityProperties

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SecurityInterceptor(properties))
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
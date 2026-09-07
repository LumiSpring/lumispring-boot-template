package com.lumispring.framework.web.config

import com.lumispring.framework.web.handler.DataInitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration :WebMvcConfigurer {

    /**
     * 配置拦截器
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(DataInitInterceptor())
            .addPathPatterns("/**") // 匹配所有路径
        super.addInterceptors(registry)
    }

    /**
     * 跨域配置
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        // 覆盖所有请求
        registry.addMapping("/**")
            .allowCredentials(true) // 允许发送cookie
            .allowedOriginPatterns("*") // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
            .allowedMethods("GET", "POST", "DELETE", "PUT","PATCH") // 放行哪些请求方式
            .allowedHeaders("*")    // 允许跨域访问的响应头
            .exposedHeaders("*")
    }

    /**
     * 添加静态资源映射
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 防止api文档 404问题
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
        registry.addResourceHandler("/v3/api-docs/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
    }
}
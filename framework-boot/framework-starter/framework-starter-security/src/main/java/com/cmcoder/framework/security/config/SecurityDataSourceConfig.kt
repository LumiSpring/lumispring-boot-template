package com.lumispring.framework.security.config

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import com.lumispring.framework.base.model.ServiceException
import com.lumispring.framework.database.mysql.config.MyBatisPlusConfiguration
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * Security 模块数据源配置
 * 独立配置 security 相关表的数据源
 * 如果没有配置 security 数据源，则使用默认数据源
 */
@Configuration
@MapperScan(
    basePackages = ["com.lumispring.framework.security.mapper"],
    sqlSessionFactoryRef = "securitySqlSessionFactory"
)
class SecurityDataSourceConfig {

    @Autowired(required = false)
    private var metaObjectHandler: MetaObjectHandler? = null

    /**
     * Security 数据源配置属性
     * 仅在配置了 spring.datasource.security 时才创建
     * @ConfigurationProperties注解将spring.datasource.security.*的属性绑定到HikariConfig对象
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.security")
    @ConditionalOnProperty(prefix = "spring.datasource.security", name = ["jdbc-url"])
    fun securityHikariConfig(): HikariConfig {
        return HikariConfig()
    }

    /**
     * Security 独立数据源
     * 仅在配置了 security 数据源时才创建
     */
    @Bean(name = ["securityDataSource"])
    @ConditionalOnProperty(prefix = "spring.datasource.security", name = ["jdbc-url"])
    fun securityDataSource(): DataSource {
        return HikariDataSource(securityHikariConfig())
    }

    /**
     * Security SqlSessionFactory
     * 如果有独立的 security 数据源则使用，否则使用默认数据源
     */
    @Bean(name = ["securitySqlSessionFactory"])
    fun securitySqlSessionFactory(
        @Qualifier("securityDataSource") securityDataSource: DataSource?,
        @Qualifier("dataSource") defaultDataSource: DataSource
    ): SqlSessionFactory {
        // 优先使用 security 数据源，否则使用默认数据源
        val dataSource = securityDataSource ?: defaultDataSource
        
        val sqlSessionFactory = MybatisSqlSessionFactoryBean()
        sqlSessionFactory.setDataSource(dataSource)
        
        // 设置 Mybatis基础配置
        val configuration = MybatisConfiguration()
        configuration.isMapUnderscoreToCamelCase = true
        configuration.isCallSettersOnNulls = true
        sqlSessionFactory.configuration = configuration
        
        // 设置 MetaObjectHandler（自动填充处理器）
        metaObjectHandler?.let {
            sqlSessionFactory.setPlugins(com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor().apply {
                // 这里不添加拦截器，只用于设置全局配置
            })
        }
        
        // 设置 Mapper XML 位置
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath*:/mapper/**/*.xml")
        sqlSessionFactory.setMapperLocations(*resources)
        
        // 设置 MetaObjectHandler（关键：自动填充字段）
        metaObjectHandler?.let { handler ->
            val globalConfig = GlobalConfig()
            globalConfig.metaObjectHandler = handler
            sqlSessionFactory.setGlobalConfig(globalConfig)
        }
        
        return sqlSessionFactory.getObject() ?: throw ServiceException("创建Security SqlSessionFactory失败")
    }

    /**
     * Security 事务管理器
     * 如果有独立的 security 数据源则使用，否则使用默认数据源的事务管理器
     */
    @Bean(name = ["securityTransactionManager"])
    fun securityTransactionManager(
        @Qualifier("securityDataSource") securityDataSource: DataSource?,
        @Qualifier("dataSource") defaultDataSource: DataSource
    ): PlatformTransactionManager {
        // 优先使用 security 数据源，否则使用默认数据源
        val dataSource = securityDataSource ?: defaultDataSource
        return DataSourceTransactionManager(dataSource)
    }
}

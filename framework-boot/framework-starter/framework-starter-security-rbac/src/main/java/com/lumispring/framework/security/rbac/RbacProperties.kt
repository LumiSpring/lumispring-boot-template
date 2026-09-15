package com.lumispring.framework.security.rbac

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * RBAC 模块配置，前缀 `security.rbac`。
 */
@ConfigurationProperties("security.rbac")
class RbacProperties {

    /**
     * 启动时检测并自动创建缺失的 RBAC 表。
     */
    @NestedConfigurationProperty
    var schemaInit: SchemaInit = SchemaInit()

    class SchemaInit {
        /**
         * 是否检测并自动创建缺失的表，默认开启。
         */
        var enabled: Boolean = true

        /**
         * 表为空时是否写入内置角色、权限和开发管理员，默认开启。
         */
        var seed: Boolean = true
    }
}

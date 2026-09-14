package com.lumispring.framework.security.config

import com.lumispring.framework.database.mysql.config.MetaObjectOfCreateBy
import com.lumispring.framework.security.extension.currentUserId
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
class MybatisPlusCreateByMetaObject: MetaObjectOfCreateBy {
    override fun get(): Any? {
        return currentUserId()
    }
}
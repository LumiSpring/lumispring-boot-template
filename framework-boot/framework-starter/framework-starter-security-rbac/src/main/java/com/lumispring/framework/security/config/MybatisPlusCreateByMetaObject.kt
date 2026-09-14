package com.lumispring.framework.security.config

import com.lumispring.framework.database.mysql.config.MetaObjectOfCreateBy
import com.lumispring.framework.security.extension.currentUserId

class MybatisPlusCreateByMetaObject : MetaObjectOfCreateBy {
    override fun get(): Any? {
        return currentUserId()
    }
}

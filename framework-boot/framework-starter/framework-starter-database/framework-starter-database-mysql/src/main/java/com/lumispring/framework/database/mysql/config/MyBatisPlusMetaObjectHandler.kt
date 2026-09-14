package com.lumispring.framework.database.mysql.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import com.lumispring.framework.base.extension.getBeanOrNull
import org.apache.ibatis.reflection.MetaObject
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充 create_time, update_time, create_by, update_by 等字段
 */
class MyBatisPlusMetaObjectHandler : MetaObjectHandler {

    private val metaObjectOfCreateBy by lazy {
        getBeanOrNull(MetaObjectOfCreateBy::class.java)
    }

    /**
     * 插入时自动填充
     */
    override fun insertFill(metaObject: MetaObject) {
        val now = LocalDateTime.now()

        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "create_time", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "created_time", LocalDateTime::class.java, now)
//        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "update_time", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime::class.java, now)
        this.strictInsertFill(metaObject, "updated_time", LocalDateTime::class.java, now)

        // 填充用户字段
        val currentUserId = metaObjectOfCreateBy?.get()
        if (currentUserId != null) {
            this.setFieldValByName("createBy", currentUserId, metaObject)
            this.setFieldValByName("create_by", currentUserId, metaObject)
            this.setFieldValByName("createdBy", currentUserId, metaObject)
            this.setFieldValByName("created_by", currentUserId, metaObject)

            this.setFieldValByName("updateBy", currentUserId, metaObject)
            this.setFieldValByName("update_by", currentUserId, metaObject)
            this.setFieldValByName("updatedBy", currentUserId, metaObject)
            this.setFieldValByName("updated_by", currentUserId, metaObject)
        }
    }

    /**
     * 更新时自动填充
     */
    override fun updateFill(metaObject: MetaObject) {
        val now = LocalDateTime.now()
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::class.java, now)
        this.strictUpdateFill(metaObject, "update_time", LocalDateTime::class.java, now)
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime::class.java, now)
        this.strictUpdateFill(metaObject, "updated_time", LocalDateTime::class.java, now)

        // 填充更新用户
        val currentUserId = metaObjectOfCreateBy?.get()
        if (currentUserId != null) {
            this.setFieldValByName("updateBy", currentUserId, metaObject)
            this.setFieldValByName("update_by", currentUserId, metaObject)
            this.setFieldValByName("updatedBy", currentUserId, metaObject)
            this.setFieldValByName("updated_by", currentUserId, metaObject)
        }
    }
}

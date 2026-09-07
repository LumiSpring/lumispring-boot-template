package com.lumispring.framework.database.mysql.config

/**
 * 对外暴露的扩展接口，用于获取当前创建者信息
 */
interface MetaObjectOfCreateBy {
    fun get(): Any?
}
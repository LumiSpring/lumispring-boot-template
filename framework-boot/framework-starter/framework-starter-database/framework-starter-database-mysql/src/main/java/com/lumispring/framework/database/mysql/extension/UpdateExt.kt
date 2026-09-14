package com.lumispring.framework.database.mysql.extension

import com.lumispring.framework.base.extension.SMap

@JvmOverloads
fun updateSql(
    sql: String,
    params: SMap? = null):Int{
    val mapParams = params ?: mapOf()
    val namedParameterJdbcTemplate = getJdbcTemplate()
    return namedParameterJdbcTemplate.update(sql, mapParams)
}

@JvmOverloads
fun batchUpdateSql(
    sql: String,
    batchParams: List<SMap>? = null):IntArray{
    val batchMapParams = batchParams ?: listOf()
    val namedParameterJdbcTemplate = getJdbcTemplate()
    return namedParameterJdbcTemplate.batchUpdate(sql, batchMapParams.toTypedArray())
}
package com.lumispring.framework.database.mysql.extension

import com.lumispring.framework.base.extension.SMap
import com.lumispring.framework.base.extension.toCamelCase
import com.lumispring.framework.base.extension.toObject
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.JdbcUtils
import java.sql.ResultSet
import java.sql.ResultSetMetaData


class DefaultColumnMapRowMapper(private val isCamelCase: Boolean = false) : ColumnMapRowMapper() {
    override fun getColumnKey(columnName: String): String {
        return if (isCamelCase) columnName.toCamelCase() else columnName
    }

    override fun mapRow(rs: ResultSet, rowNum: Int): MutableMap<String, Any> {
        val rsmd: ResultSetMetaData = rs.metaData
        val columnCount: Int = rsmd.columnCount
        val mapOfColumnValues = createColumnMap(columnCount)
        for (i in 1..columnCount) {
            val column: String = JdbcUtils.lookupColumnName(rsmd, i)
            mapOfColumnValues[getColumnKey(column)] = getColumnValue(rs, i)
        }
        return mapOfColumnValues
    }
}

/**
 * 查询sql
 * @param sql sql语句，eg: select * from user where id = :id
 * @param params 参数 , eg: mapOf("id" to 1)
 * @param isCamelCase 是否转换为驼峰命名
 * @param rowMapper 行映射
 */
@JvmOverloads
fun selectSqlMaps(
    sql: String,
    params: SMap? = null,
    isCamelCase: Boolean = true,
    rowMapper: RowMapper<SMap> = DefaultColumnMapRowMapper(isCamelCase)
): List<SMap> {
    val mapParams = params ?: mapOf()
    val namedParameterJdbcTemplate = getJdbcTemplate()
    return namedParameterJdbcTemplate.query(sql, mapParams, rowMapper)
}

/**
 * 查询sql，返回第一条数据
 * @param sql sql语句，eg: select * from user where id = :id
 * @param params 参数 , eg: mapOf("id" to 1)
 * @param isCamelCase 是否转换为驼峰命名
 * @param rowMapper 行映射
 */
@JvmOverloads
fun selectSqlOneMap(
    sql: String,
    params: SMap? = null,
    isCamelCase: Boolean = true,
    rowMapper: RowMapper<SMap> = DefaultColumnMapRowMapper(isCamelCase)
): SMap? {
    return selectSqlMaps(sql, params, isCamelCase, rowMapper).firstOrNull()
}

/**
 * 查询sql，返回指定类型的数据
 * @param sql sql语句，eg: select * from user where id = :id
 * @param params 参数 , eg: mapOf("id" to 1)
 * @param isCamelCase 是否转换为驼峰命名
 * @param rowMapper 行映射
 */
@JvmOverloads
inline fun <reified T> selectSqlObjs(
    sql: String,
    params: SMap? = null,
    isCamelCase: Boolean = true,
    rowMapper: RowMapper<SMap> = DefaultColumnMapRowMapper(isCamelCase)
): List<T> {
    val res = selectSqlMaps(sql, params, isCamelCase, rowMapper)
    return res.toObject<List<T>>() ?: listOf()
}

/**
 * 查询sql，返回指定类型的数据
 * @param sql sql语句，eg: select * from user where id = :id
 * @param params 参数 , eg: mapOf("id" to 1)
 * @param isCamelCase 是否转换为驼峰命名
 * @param rowMapper 行映射
 */
@JvmOverloads
inline fun <reified T> selectSqlOneObj(
    sql: String,
    params: SMap? = null,
    isCamelCase: Boolean = true,
    rowMapper: RowMapper<SMap> = DefaultColumnMapRowMapper(isCamelCase)
): T? {
    return selectSqlOneMap(sql, params, isCamelCase, rowMapper)?.toObject<T>()
}
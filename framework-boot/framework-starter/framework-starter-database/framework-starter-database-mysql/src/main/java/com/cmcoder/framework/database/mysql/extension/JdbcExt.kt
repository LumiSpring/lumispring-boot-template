package com.lumispring.framework.database.mysql.extension

import com.lumispring.framework.base.extension.getBean
import com.lumispring.framework.base.extension.isNotNull
import com.lumispring.framework.base.extension.isNull
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource


@PublishedApi   // 方便生成文档时可见
internal var namedParameterJdbcTemplate: NamedParameterJdbcTemplate? = null

/**
 * 获取Jdbc
 */
@JvmOverloads
fun getJdbcTemplate(dataSource: DataSource? = null):NamedParameterJdbcTemplate{
    if (dataSource.isNotNull()){
        return NamedParameterJdbcTemplate(dataSource)
    }
    if (namedParameterJdbcTemplate.isNull()){
        namedParameterJdbcTemplate = getBean(NamedParameterJdbcTemplate::class.java)
    }
    return namedParameterJdbcTemplate!!
}
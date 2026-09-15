package com.lumispring.framework.security.rbac

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import java.sql.Connection
import javax.sql.DataSource

/**
 * 检测当前库是否具备 RBAC 表；缺失时执行 [SCHEMA_SCRIPT]，空表时按需写入 [DATA_SCRIPT]。
 */
class RbacSchemaDatabaseInitializer(
    private val dataSource: DataSource,
    private val properties: RbacProperties
) : InitializingBean {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun afterPropertiesSet() {
        initialize()
    }

    fun initialize() {
        if (!properties.schemaInit.enabled) {
            return
        }
        val missing = dataSource.connection.use { connection ->
            REQUIRED_TABLES.filterNot { tableExists(connection, it) }
        }
        if (missing.isNotEmpty()) {
            logger.info("RBAC 缺少数据表 {}，开始自动建表", missing.joinToString())
            executeScript(SCHEMA_SCRIPT)
        }
        if (properties.schemaInit.seed && dataSource.connection.use { needsSeed(it) }) {
            logger.info("RBAC 表为空，开始写入内置角色、权限与开发管理员")
            executeScript(DATA_SCRIPT)
        }
    }

    private fun needsSeed(connection: Connection): Boolean {
        return REQUIRED_TABLES.all { tableExists(connection, it) } &&
            (isEmpty(connection, "sys_role") || isEmpty(connection, "sys_permission") || isEmpty(connection, "sys_user"))
    }

    private fun tableExists(connection: Connection, table: String): Boolean {
        connection.prepareStatement(TABLE_EXISTS_SQL).use { statement ->
            statement.setString(1, table)
            statement.executeQuery().use { resultSet ->
                return resultSet.next() && resultSet.getInt(1) > 0
            }
        }
    }

    private fun isEmpty(connection: Connection, table: String): Boolean {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT 1 FROM `$table` LIMIT 1").use { resultSet ->
                return !resultSet.next()
            }
        }
    }

    private fun executeScript(location: String) {
        val populator = ResourceDatabasePopulator().apply {
            setSqlScriptEncoding(Charsets.UTF_8.name())
            setContinueOnError(false)
            setSeparator(";")
            addScript(ClassPathResource(location))
        }
        DatabasePopulatorUtils.execute(populator, dataSource)
    }

    companion object {
        const val SCHEMA_SCRIPT = "db/schema.sql"
        const val DATA_SCRIPT = "db/data.sql"

        val REQUIRED_TABLES = listOf(
            "sys_user",
            "sys_role",
            "sys_user_role",
            "sys_permission",
            "sys_role_permission",
            "sys_user_permission",
            "sys_operation_log"
        )

        private const val TABLE_EXISTS_SQL = """
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = ?
        """
    }
}

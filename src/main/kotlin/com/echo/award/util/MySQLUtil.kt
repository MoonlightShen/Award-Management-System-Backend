package com.echo.award.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.sql.*

/**
 * @author MoonlightShen
 * @data 2024/9/5 15:27
 **/
@Component
class MySQLUtil {

    @Value("\${spring.datasource.url}")
    private lateinit var jdbcUrl: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    @Value("\${spring.datasource.driver-class-name}")
    private lateinit var driverClassName: String

    @Value("\${spring.datasource.hikari.maximum-pool-size}")
    private var maximumPoolSize: Int = 999

    @Value("\${spring.datasource.hikari.connection-timeout}")
    private var connectionTimeout: Long = 99999999

//    @PostConstruct
//    fun init() {
//        val config = HikariConfig().apply {
//            jdbcUrl = this@MySQLUtil.jdbcUrl
//            username = this@MySQLUtil.username
//            password = this@MySQLUtil.password
//            driverClassName = this@MySQLUtil.driverClassName
//            maximumPoolSize = this@MySQLUtil.maximumPoolSize
//            connectionTimeout = this@MySQLUtil.connectionTimeout
//        }
//        dataSource = HikariDataSource(config)
//    }

    init {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(this@MySQLUtil.jdbcUrl, this@MySQLUtil.username, this@MySQLUtil.password)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 执行查询
    fun executeQuery(query: String): ResultSet? {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null

        try {
            connection = getConnection()
            statement = connection?.createStatement()
            resultSet = statement?.executeQuery(query)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultSet
    }

    // 执行更新（INSERT, UPDATE, DELETE）
    fun executeUpdate(query: String): Int {
        var connection: Connection? = null
        var statement: Statement? = null
        var result = 0

        try {
            connection = getConnection()
            statement = connection?.createStatement()
            result = statement?.executeUpdate(query) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 关闭连接
            statement?.close()
            connection?.close()
        }

        return result
    }

    fun executeUpdateWithGeneratedKey(query: String, vararg params: Any): Long? {
        val connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var generatedKey: Long? = null

        try {
            // 获取数据库连接
            val connection = getConnection()

            // 准备 SQL 语句并设置 `RETURN_GENERATED_KEYS` 选项
            preparedStatement = connection!!.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)

            // 设置参数
            params.forEachIndexed { index, param ->
                preparedStatement.setObject(index + 1, param)
            }

            // 执行更新操作
            val rowsInserted = preparedStatement.executeUpdate()

            // 检查是否插入成功，并获取生成的 ID
            if (rowsInserted > 0) {
                val rs: ResultSet = preparedStatement.generatedKeys
                if (rs.next()) {
                    generatedKey = rs.getLong(1)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            preparedStatement?.close()
            connection?.close()
        }

        return generatedKey
    }

    // 关闭连接
    fun closeResources(connection: Connection?, statement: Statement?, resultSet: ResultSet?) {
        try {
            resultSet?.close()
            statement?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
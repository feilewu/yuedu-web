package io.legado.server.db

import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val logger = LoggerFactory.getLogger(Database::class.java)
    private var connection: Connection? = null

    fun init(dbPath: String) {
        Class.forName("org.sqlite.JDBC")
        val path = File(dbPath, "legado.db").absolutePath
        connection = DriverManager.getConnection("jdbc:sqlite:$path")
        connection?.let { conn ->
            conn.autoCommit = true
            val stmt = conn.createStatement()
            stmt.execute("PRAGMA journal_mode=WAL")
            stmt.execute("PRAGMA foreign_keys=ON")
            stmt.close()
        }
        Tables.createTables()
        logger.info("Database initialized at $path")
    }

    fun getConnection(): Connection =
        connection ?: throw IllegalStateException("Database not initialized")

    fun close() {
        connection?.close()
        connection = null
    }
}

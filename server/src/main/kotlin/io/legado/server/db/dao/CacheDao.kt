package io.legado.server.db.dao
import io.legado.server.db.Database
import io.legado.server.model.entity.Cache
object CacheDao {
    private fun mapRow(rs: java.sql.ResultSet): Cache {
        return Cache(
            key = rs.getString("key") ?: "",
            value = rs.getString("value"),
            deadline = rs.getLong("deadline")
        )
    }
    fun get(key: String): Cache? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM caches WHERE key = ?")
        stmt.setString(1, key)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) mapRow(rs) else null
        rs.close()
        stmt.close()
        return result
    }
    fun put(key: String, value: String?) {
        val conn = Database.getConnection()
        val sql = "INSERT OR REPLACE INTO caches (key, value, deadline) VALUES (?,?,?)"
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.setLong(3, System.currentTimeMillis())
            stmt.executeUpdate()
        }
    }
    fun put(key: String, value: String?, deadline: Long) {
        val conn = Database.getConnection()
        val sql = "INSERT OR REPLACE INTO caches (key, value, deadline) VALUES (?,?,?)"
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.setLong(3, deadline)
            stmt.executeUpdate()
        }
    }
    fun delete(key: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM caches WHERE key = ?").use { stmt ->
            stmt.setString(1, key)
            stmt.executeUpdate()
        }
    }
}

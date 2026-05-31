package io.legado.server.db.dao
import io.legado.server.db.Database
import io.legado.server.model.entity.ReplaceRule
object ReplaceRuleDao {
    private fun mapRow(rs: java.sql.ResultSet): ReplaceRule {
        return ReplaceRule(
            id = rs.getLong("id"),
            name = rs.getString("name") ?: "",
            group = rs.getString("group_"),
            pattern = rs.getString("pattern") ?: "",
            replacement = rs.getString("replacement") ?: "",
            scope = rs.getString("scope"),
            scopeTitle = rs.getInt("scopeTitle") != 0,
            scopeContent = rs.getInt("scopeContent") != 0,
            excludeScope = rs.getString("excludeScope"),
            isEnabled = rs.getInt("isEnabled") != 0,
            isRegex = rs.getInt("isRegex") != 0,
            timeoutMillisecond = rs.getLong("timeoutMillisecond"),
            order = rs.getInt("order_")
        )
    }
    fun findAll(): List<ReplaceRule> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM replace_rules")
        val list = mutableListOf<ReplaceRule>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun save(rule: ReplaceRule) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO replace_rules (
                id, name, group_, pattern, replacement, scope, scopeTitle, scopeContent,
                excludeScope, isEnabled, isRegex, timeoutMillisecond, order_
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, rule.id)
            stmt.setString(2, rule.name)
            stmt.setString(3, rule.group)
            stmt.setString(4, rule.pattern)
            stmt.setString(5, rule.replacement)
            stmt.setString(6, rule.scope)
            stmt.setInt(7, if (rule.scopeTitle) 1 else 0)
            stmt.setInt(8, if (rule.scopeContent) 1 else 0)
            stmt.setString(9, rule.excludeScope)
            stmt.setInt(10, if (rule.isEnabled) 1 else 0)
            stmt.setInt(11, if (rule.isRegex) 1 else 0)
            stmt.setLong(12, rule.timeoutMillisecond)
            stmt.setInt(13, rule.order)
            stmt.executeUpdate()
        }
    }
    fun delete(rule: ReplaceRule) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM replace_rules WHERE id = ?").use { stmt ->
            stmt.setLong(1, rule.id)
            stmt.executeUpdate()
        }
    }
}

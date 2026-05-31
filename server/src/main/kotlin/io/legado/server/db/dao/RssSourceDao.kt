package io.legado.server.db.dao
import io.legado.server.db.Database
import io.legado.server.model.entity.RssSource
object RssSourceDao {
    private fun mapRow(rs: java.sql.ResultSet): RssSource {
        val cookieJar = rs.getObject("enabledCookieJar") as? Int
        return RssSource(
            sourceUrl = rs.getString("sourceUrl") ?: "",
            sourceName = rs.getString("sourceName") ?: "",
            sourceIcon = rs.getString("sourceIcon") ?: "",
            sourceGroup = rs.getString("sourceGroup"),
            sourceComment = rs.getString("sourceComment"),
            enabled = rs.getInt("enabled") != 0,
            variableComment = rs.getString("variableComment"),
            jsLib = rs.getString("jsLib"),
            enabledCookieJar = if (cookieJar != null) cookieJar != 0 else null,
            concurrentRate = rs.getString("concurrentRate"),
            header = rs.getString("header"),
            loginUrl = rs.getString("loginUrl"),
            loginUi = rs.getString("loginUi"),
            loginCheckJs = rs.getString("loginCheckJs"),
            coverDecodeJs = rs.getString("coverDecodeJs"),
            sortUrl = rs.getString("sortUrl"),
            singleUrl = rs.getInt("singleUrl") != 0,
            articleStyle = rs.getInt("articleStyle"),
            ruleArticles = rs.getString("ruleArticles"),
            ruleNextPage = rs.getString("ruleNextPage"),
            ruleTitle = rs.getString("ruleTitle"),
            rulePubDate = rs.getString("rulePubDate"),
            ruleDescription = rs.getString("ruleDescription"),
            ruleImage = rs.getString("ruleImage"),
            ruleLink = rs.getString("ruleLink"),
            ruleContent = rs.getString("ruleContent"),
            contentWhitelist = rs.getString("contentWhitelist"),
            contentBlacklist = rs.getString("contentBlacklist"),
            shouldOverrideUrlLoading = rs.getString("shouldOverrideUrlLoading"),
            style = rs.getString("style"),
            enableJs = rs.getInt("enableJs") != 0,
            loadWithBaseUrl = rs.getInt("loadWithBaseUrl") != 0,
            injectJs = rs.getString("injectJs"),
            lastUpdateTime = rs.getLong("lastUpdateTime"),
            customOrder = rs.getInt("customOrder")
        )
    }
    fun findAll(): List<RssSource> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM rss_sources")
        val list = mutableListOf<RssSource>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findAllEnabled(): List<RssSource> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM rss_sources WHERE enabled = 1")
        val rs = stmt.executeQuery()
        val list = mutableListOf<RssSource>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findByUrl(url: String): RssSource? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM rss_sources WHERE sourceUrl = ?")
        stmt.setString(1, url)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) mapRow(rs) else null
        rs.close()
        stmt.close()
        return result
    }
    fun save(source: RssSource) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO rss_sources (
                sourceUrl, sourceName, sourceIcon, sourceGroup, sourceComment,
                enabled, variableComment, jsLib, enabledCookieJar, concurrentRate,
                header, loginUrl, loginUi, loginCheckJs, coverDecodeJs, sortUrl,
                singleUrl, articleStyle, ruleArticles, ruleNextPage, ruleTitle,
                rulePubDate, ruleDescription, ruleImage, ruleLink, ruleContent,
                contentWhitelist, contentBlacklist, shouldOverrideUrlLoading,
                style, enableJs, loadWithBaseUrl, injectJs, lastUpdateTime, customOrder
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, source.sourceUrl)
            stmt.setString(2, source.sourceName)
            stmt.setString(3, source.sourceIcon)
            stmt.setString(4, source.sourceGroup)
            stmt.setString(5, source.sourceComment)
            stmt.setInt(6, if (source.enabled) 1 else 0)
            stmt.setString(7, source.variableComment)
            stmt.setString(8, source.jsLib)
            val cookieJar = source.enabledCookieJar
            if (cookieJar != null) stmt.setInt(9, if (cookieJar) 1 else 0)
            else stmt.setNull(9, java.sql.Types.INTEGER)
            stmt.setString(10, source.concurrentRate)
            stmt.setString(11, source.header)
            stmt.setString(12, source.loginUrl)
            stmt.setString(13, source.loginUi)
            stmt.setString(14, source.loginCheckJs)
            stmt.setString(15, source.coverDecodeJs)
            stmt.setString(16, source.sortUrl)
            stmt.setInt(17, if (source.singleUrl) 1 else 0)
            stmt.setInt(18, source.articleStyle)
            stmt.setString(19, source.ruleArticles)
            stmt.setString(20, source.ruleNextPage)
            stmt.setString(21, source.ruleTitle)
            stmt.setString(22, source.rulePubDate)
            stmt.setString(23, source.ruleDescription)
            stmt.setString(24, source.ruleImage)
            stmt.setString(25, source.ruleLink)
            stmt.setString(26, source.ruleContent)
            stmt.setString(27, source.contentWhitelist)
            stmt.setString(28, source.contentBlacklist)
            stmt.setString(29, source.shouldOverrideUrlLoading)
            stmt.setString(30, source.style)
            stmt.setInt(31, if (source.enableJs) 1 else 0)
            stmt.setInt(32, if (source.loadWithBaseUrl) 1 else 0)
            stmt.setString(33, source.injectJs)
            stmt.setLong(34, source.lastUpdateTime)
            stmt.setInt(35, source.customOrder)
            stmt.executeUpdate()
        }
    }
    fun delete(url: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM rss_sources WHERE sourceUrl = ?").use { stmt ->
            stmt.setString(1, url)
            stmt.executeUpdate()
        }
    }
}

package io.legado.server.db.dao
import com.google.gson.Gson
import io.legado.server.db.Database
import io.legado.server.model.entity.BookSource
import io.legado.server.model.entity.rule.BookInfoRule
import io.legado.server.model.entity.rule.ContentRule
import io.legado.server.model.entity.rule.ExploreRule
import io.legado.server.model.entity.rule.ReviewRule
import io.legado.server.model.entity.rule.SearchRule
import io.legado.server.model.entity.rule.TocRule
object BookSourceDao {
    private val gson = Gson()
    private fun mapRow(rs: java.sql.ResultSet): BookSource {
        val ruleExploreJson = rs.getString("ruleExplore")
        val ruleSearchJson = rs.getString("ruleSearch")
        val ruleBookInfoJson = rs.getString("ruleBookInfo")
        val ruleTocJson = rs.getString("ruleToc")
        val ruleContentJson = rs.getString("ruleContent")
        val ruleReviewJson = rs.getString("ruleReview")
        val cookieJar = rs.getObject("enabledCookieJar") as? Int
        return BookSource(
            bookSourceUrl = rs.getString("bookSourceUrl") ?: "",
            bookSourceName = rs.getString("bookSourceName") ?: "",
            bookSourceGroup = rs.getString("bookSourceGroup"),
            bookSourceType = rs.getInt("bookSourceType"),
            bookUrlPattern = rs.getString("bookUrlPattern"),
            customOrder = rs.getInt("customOrder"),
            enabled = rs.getInt("enabled") != 0,
            enabledExplore = rs.getInt("enabledExplore") != 0,
            jsLib = rs.getString("jsLib"),
            enabledCookieJar = if (cookieJar != null) cookieJar != 0 else null,
            concurrentRate = rs.getString("concurrentRate"),
            header = rs.getString("header"),
            loginUrl = rs.getString("loginUrl"),
            loginUi = rs.getString("loginUi"),
            loginCheckJs = rs.getString("loginCheckJs"),
            coverDecodeJs = rs.getString("coverDecodeJs"),
            bookSourceComment = rs.getString("bookSourceComment"),
            variableComment = rs.getString("variableComment"),
            lastUpdateTime = rs.getLong("lastUpdateTime"),
            respondTime = rs.getLong("respondTime"),
            weight = rs.getInt("weight"),
            exploreUrl = rs.getString("exploreUrl"),
            exploreScreen = rs.getString("exploreScreen"),
            ruleExplore = if (ruleExploreJson != null) gson.fromJson(ruleExploreJson, ExploreRule::class.java) else null,
            searchUrl = rs.getString("searchUrl"),
            ruleSearch = if (ruleSearchJson != null) gson.fromJson(ruleSearchJson, SearchRule::class.java) else null,
            ruleBookInfo = if (ruleBookInfoJson != null) gson.fromJson(ruleBookInfoJson, BookInfoRule::class.java) else null,
            ruleToc = if (ruleTocJson != null) gson.fromJson(ruleTocJson, TocRule::class.java) else null,
            ruleContent = if (ruleContentJson != null) gson.fromJson(ruleContentJson, ContentRule::class.java) else null,
            eventListener = rs.getInt("eventListener") != 0,
            customButton = rs.getInt("customButton") != 0,
            ruleReview = if (ruleReviewJson != null) gson.fromJson(ruleReviewJson, ReviewRule::class.java) else null
        )
    }
    fun findAll(): List<BookSource> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM book_sources")
        val list = mutableListOf<BookSource>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findAllEnabled(): List<BookSource> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM book_sources WHERE enabled = 1")
        val rs = stmt.executeQuery()
        val list = mutableListOf<BookSource>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findByUrl(url: String): BookSource? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM book_sources WHERE bookSourceUrl = ?")
        stmt.setString(1, url)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) mapRow(rs) else null
        rs.close()
        stmt.close()
        return result
    }
    fun save(source: BookSource) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO book_sources (
                bookSourceUrl, bookSourceName, bookSourceGroup, bookSourceType, bookUrlPattern,
                customOrder, enabled, enabledExplore, jsLib, enabledCookieJar, concurrentRate,
                header, loginUrl, loginUi, loginCheckJs, coverDecodeJs, bookSourceComment,
                variableComment, lastUpdateTime, respondTime, weight, exploreUrl, exploreScreen,
                ruleExplore, searchUrl, ruleSearch, ruleBookInfo, ruleToc, ruleContent,
                eventListener, customButton, ruleReview
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, source.bookSourceUrl)
            stmt.setString(2, source.bookSourceName)
            stmt.setString(3, source.bookSourceGroup)
            stmt.setInt(4, source.bookSourceType)
            stmt.setString(5, source.bookUrlPattern)
            stmt.setInt(6, source.customOrder)
            stmt.setInt(7, if (source.enabled) 1 else 0)
            stmt.setInt(8, if (source.enabledExplore) 1 else 0)
            stmt.setString(9, source.jsLib)
            val cookieJar = source.enabledCookieJar
            if (cookieJar != null) stmt.setInt(10, if (cookieJar) 1 else 0)
            else stmt.setNull(10, java.sql.Types.INTEGER)
            stmt.setString(11, source.concurrentRate)
            stmt.setString(12, source.header)
            stmt.setString(13, source.loginUrl)
            stmt.setString(14, source.loginUi)
            stmt.setString(15, source.loginCheckJs)
            stmt.setString(16, source.coverDecodeJs)
            stmt.setString(17, source.bookSourceComment)
            stmt.setString(18, source.variableComment)
            stmt.setLong(19, source.lastUpdateTime)
            stmt.setLong(20, source.respondTime)
            stmt.setInt(21, source.weight)
            stmt.setString(22, source.exploreUrl)
            stmt.setString(23, source.exploreScreen)
            stmt.setString(24, if (source.ruleExplore != null) gson.toJson(source.ruleExplore) else null)
            stmt.setString(25, source.searchUrl)
            stmt.setString(26, if (source.ruleSearch != null) gson.toJson(source.ruleSearch) else null)
            stmt.setString(27, if (source.ruleBookInfo != null) gson.toJson(source.ruleBookInfo) else null)
            stmt.setString(28, if (source.ruleToc != null) gson.toJson(source.ruleToc) else null)
            stmt.setString(29, if (source.ruleContent != null) gson.toJson(source.ruleContent) else null)
            stmt.setInt(30, if (source.eventListener) 1 else 0)
            stmt.setInt(31, if (source.customButton) 1 else 0)
            stmt.setString(32, if (source.ruleReview != null) gson.toJson(source.ruleReview) else null)
            stmt.executeUpdate()
        }
    }
    fun delete(url: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM book_sources WHERE bookSourceUrl = ?").use { stmt ->
            stmt.setString(1, url)
            stmt.executeUpdate()
        }
    }
}

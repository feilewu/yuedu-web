package io.legado.server.db.dao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.legado.server.db.Database
import io.legado.server.model.entity.Book
object BookDao {
    private val gson = Gson()
    private fun mapRow(rs: java.sql.ResultSet): Book {
        val readConfigJson = rs.getString("readConfig")
        val downloadUrlsJson = rs.getString("downloadUrls")
        return Book(
            bookUrl = rs.getString("bookUrl") ?: "",
            tocUrl = rs.getString("tocUrl") ?: "",
            origin = rs.getString("origin") ?: "",
            originName = rs.getString("originName") ?: "",
            name = rs.getString("name") ?: "",
            author = rs.getString("author") ?: "",
            kind = rs.getString("kind"),
            customTag = rs.getString("customTag"),
            coverUrl = rs.getString("coverUrl"),
            customCoverUrl = rs.getString("customCoverUrl"),
            intro = rs.getString("intro"),
            customIntro = rs.getString("customIntro"),
            charset = rs.getString("charset"),
            type = rs.getInt("type"),
            group = rs.getLong("group_"),
            latestChapterTitle = rs.getString("latestChapterTitle"),
            latestChapterTime = rs.getLong("latestChapterTime"),
            lastCheckTime = rs.getLong("lastCheckTime"),
            lastCheckCount = rs.getInt("lastCheckCount"),
            totalChapterNum = rs.getInt("totalChapterNum"),
            durChapterTitle = rs.getString("durChapterTitle"),
            durChapterIndex = rs.getInt("durChapterIndex"),
            durChapterPos = rs.getInt("durChapterPos"),
            durChapterTime = rs.getLong("durChapterTime"),
            wordCount = rs.getString("wordCount"),
            canUpdate = rs.getInt("canUpdate") != 0,
            order = rs.getInt("order_"),
            originOrder = rs.getInt("originOrder"),
            variable = rs.getString("variable"),
            readConfig = if (readConfigJson != null) gson.fromJson(readConfigJson, Book.ReadConfig::class.java) else null,
            syncTime = rs.getLong("syncTime")
        ).apply {
            infoHtml = rs.getString("infoHtml")
            tocHtml = rs.getString("tocHtml")
            if (downloadUrlsJson != null) {
                downloadUrls = gson.fromJson(downloadUrlsJson, object : TypeToken<List<String>>() {}.type)
            }
        }
    }
    fun findAll(): List<Book> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM books")
        val list = mutableListOf<Book>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findByUrl(bookUrl: String): Book? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM books WHERE bookUrl = ?")
        stmt.setString(1, bookUrl)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) mapRow(rs) else null
        rs.close()
        stmt.close()
        return result
    }
    fun save(book: Book) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO books (
                bookUrl, tocUrl, origin, originName, name, author, kind, customTag,
                coverUrl, customCoverUrl, intro, customIntro, charset, type, group_,
                latestChapterTitle, latestChapterTime, lastCheckTime, lastCheckCount,
                totalChapterNum, durChapterTitle, durChapterIndex, durChapterPos,
                durChapterTime, wordCount, canUpdate, order_, originOrder, variable,
                readConfig, syncTime, infoHtml, tocHtml, downloadUrls
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, book.bookUrl)
            stmt.setString(2, book.tocUrl)
            stmt.setString(3, book.origin)
            stmt.setString(4, book.originName)
            stmt.setString(5, book.name)
            stmt.setString(6, book.author)
            stmt.setString(7, book.kind)
            stmt.setString(8, book.customTag)
            stmt.setString(9, book.coverUrl)
            stmt.setString(10, book.customCoverUrl)
            stmt.setString(11, book.intro)
            stmt.setString(12, book.customIntro)
            stmt.setString(13, book.charset)
            stmt.setInt(14, book.type)
            stmt.setLong(15, book.group)
            stmt.setString(16, book.latestChapterTitle)
            stmt.setLong(17, book.latestChapterTime)
            stmt.setLong(18, book.lastCheckTime)
            stmt.setInt(19, book.lastCheckCount)
            stmt.setInt(20, book.totalChapterNum)
            stmt.setString(21, book.durChapterTitle)
            stmt.setInt(22, book.durChapterIndex)
            stmt.setInt(23, book.durChapterPos)
            stmt.setLong(24, book.durChapterTime)
            stmt.setString(25, book.wordCount)
            stmt.setInt(26, if (book.canUpdate) 1 else 0)
            stmt.setInt(27, book.order)
            stmt.setInt(28, book.originOrder)
            stmt.setString(29, book.variable)
            stmt.setString(30, if (book.readConfig != null) gson.toJson(book.readConfig) else null)
            stmt.setLong(31, book.syncTime)
            stmt.setString(32, book.infoHtml)
            stmt.setString(33, book.tocHtml)
            stmt.setString(34, if (book.downloadUrls != null) gson.toJson(book.downloadUrls) else null)
            stmt.executeUpdate()
        }
    }
    fun delete(bookUrl: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM books WHERE bookUrl = ?").use { stmt ->
            stmt.setString(1, bookUrl)
            stmt.executeUpdate()
        }
    }
}

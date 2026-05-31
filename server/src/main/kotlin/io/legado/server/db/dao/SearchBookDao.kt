package io.legado.server.db.dao
import io.legado.server.db.Database
import io.legado.server.model.entity.SearchBook
object SearchBookDao {
    private fun mapRow(rs: java.sql.ResultSet): SearchBook {
        return SearchBook(
            bookUrl = rs.getString("bookUrl") ?: "",
            origin = rs.getString("origin") ?: "",
            originName = rs.getString("originName") ?: "",
            type = rs.getInt("type"),
            name = rs.getString("name") ?: "",
            author = rs.getString("author") ?: "",
            kind = rs.getString("kind"),
            coverUrl = rs.getString("coverUrl"),
            intro = rs.getString("intro"),
            wordCount = rs.getString("wordCount"),
            latestChapterTitle = rs.getString("latestChapterTitle"),
            tocUrl = rs.getString("tocUrl") ?: "",
            time = rs.getLong("time"),
            variable = rs.getString("variable"),
            originOrder = rs.getInt("originOrder"),
            chapterWordCountText = rs.getString("chapterWordCountText"),
            chapterWordCount = rs.getInt("chapterWordCount"),
            respondTime = rs.getInt("respondTime")
        ).apply {
            infoHtml = rs.getString("infoHtml")
            tocHtml = rs.getString("tocHtml")
        }
    }
    fun findAll(): List<SearchBook> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM search_books")
        val list = mutableListOf<SearchBook>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun save(book: SearchBook) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO search_books (
                bookUrl, origin, originName, type, name, author, kind, coverUrl,
                intro, wordCount, latestChapterTitle, tocUrl, time, variable,
                originOrder, chapterWordCountText, chapterWordCount, respondTime,
                infoHtml, tocHtml
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, book.bookUrl)
            stmt.setString(2, book.origin)
            stmt.setString(3, book.originName)
            stmt.setInt(4, book.type)
            stmt.setString(5, book.name)
            stmt.setString(6, book.author)
            stmt.setString(7, book.kind)
            stmt.setString(8, book.coverUrl)
            stmt.setString(9, book.intro)
            stmt.setString(10, book.wordCount)
            stmt.setString(11, book.latestChapterTitle)
            stmt.setString(12, book.tocUrl)
            stmt.setLong(13, book.time)
            stmt.setString(14, book.variable)
            stmt.setInt(15, book.originOrder)
            stmt.setString(16, book.chapterWordCountText)
            stmt.setInt(17, book.chapterWordCount)
            stmt.setInt(18, book.respondTime)
            stmt.setString(19, book.infoHtml)
            stmt.setString(20, book.tocHtml)
            stmt.executeUpdate()
        }
    }
    fun deleteByBookUrl(bookUrl: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM search_books WHERE bookUrl = ?").use { stmt ->
            stmt.setString(1, bookUrl)
            stmt.executeUpdate()
        }
    }
    fun clear() {
        val conn = Database.getConnection()
        conn.createStatement().execute("DELETE FROM search_books")
    }
}

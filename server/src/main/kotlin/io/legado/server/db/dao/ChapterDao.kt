package io.legado.server.db.dao
import io.legado.server.db.Database
import io.legado.server.model.entity.BookChapter
object ChapterDao {
    private fun mapRow(rs: java.sql.ResultSet): BookChapter {
        return BookChapter(
            url = rs.getString("url") ?: "",
            title = rs.getString("title") ?: "",
            isVolume = rs.getInt("isVolume") != 0,
            baseUrl = rs.getString("baseUrl") ?: "",
            bookUrl = rs.getString("bookUrl") ?: "",
            index = rs.getInt("index_"),
            isVip = rs.getInt("isVip") != 0,
            isPay = rs.getInt("isPay") != 0,
            resourceUrl = rs.getString("resourceUrl"),
            tag = rs.getString("tag"),
            wordCount = rs.getString("wordCount"),
            start = rs.getObject("start_") as? Long,
            end = rs.getObject("end_") as? Long,
            startFragmentId = rs.getString("startFragmentId"),
            endFragmentId = rs.getString("endFragmentId"),
            variable = rs.getString("variable")
        ).apply {
            titleMD5 = rs.getString("titleMD5")
        }
    }
    fun findByBookUrl(bookUrl: String): List<BookChapter> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM chapters WHERE bookUrl = ? ORDER BY index_")
        stmt.setString(1, bookUrl)
        val rs = stmt.executeQuery()
        val list = mutableListOf<BookChapter>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }
    fun findByBookUrlAndIndex(bookUrl: String, index: Int): BookChapter? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT * FROM chapters WHERE bookUrl = ? AND index_ = ?")
        stmt.setString(1, bookUrl)
        stmt.setInt(2, index)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) mapRow(rs) else null
        rs.close()
        stmt.close()
        return result
    }
    fun save(chapter: BookChapter) {
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO chapters (
                url, title, isVolume, baseUrl, bookUrl, index_, isVip, isPay,
                resourceUrl, tag, wordCount, start_, end_, startFragmentId,
                endFragmentId, variable, titleMD5
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, chapter.url)
            stmt.setString(2, chapter.title)
            stmt.setInt(3, if (chapter.isVolume) 1 else 0)
            stmt.setString(4, chapter.baseUrl)
            stmt.setString(5, chapter.bookUrl)
            stmt.setInt(6, chapter.index)
            stmt.setInt(7, if (chapter.isVip) 1 else 0)
            stmt.setInt(8, if (chapter.isPay) 1 else 0)
            stmt.setString(9, chapter.resourceUrl)
            stmt.setString(10, chapter.tag)
            stmt.setString(11, chapter.wordCount)
            val startVal = chapter.start
            if (startVal != null) stmt.setLong(12, startVal) else stmt.setNull(12, java.sql.Types.INTEGER)
            val endVal = chapter.end
            if (endVal != null) stmt.setLong(13, endVal) else stmt.setNull(13, java.sql.Types.INTEGER)
            stmt.setString(14, chapter.startFragmentId)
            stmt.setString(15, chapter.endFragmentId)
            stmt.setString(16, chapter.variable)
            stmt.setString(17, chapter.titleMD5)
            stmt.executeUpdate()
        }
    }
    fun saveAll(chapters: List<BookChapter>) {
        if (chapters.isEmpty()) return
        val conn = Database.getConnection()
        val sql = """
            INSERT OR REPLACE INTO chapters (
                url, title, isVolume, baseUrl, bookUrl, index_, isVip, isPay,
                resourceUrl, tag, wordCount, start_, end_, startFragmentId,
                endFragmentId, variable, titleMD5
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()
        conn.prepareStatement(sql).use { stmt ->
            for (chapter in chapters) {
                stmt.setString(1, chapter.url)
                stmt.setString(2, chapter.title)
                stmt.setInt(3, if (chapter.isVolume) 1 else 0)
                stmt.setString(4, chapter.baseUrl)
                stmt.setString(5, chapter.bookUrl)
                stmt.setInt(6, chapter.index)
                stmt.setInt(7, if (chapter.isVip) 1 else 0)
                stmt.setInt(8, if (chapter.isPay) 1 else 0)
                stmt.setString(9, chapter.resourceUrl)
                stmt.setString(10, chapter.tag)
                stmt.setString(11, chapter.wordCount)
                val startVal = chapter.start
                if (startVal != null) stmt.setLong(12, startVal) else stmt.setNull(12, java.sql.Types.INTEGER)
                val endVal = chapter.end
                if (endVal != null) stmt.setLong(13, endVal) else stmt.setNull(13, java.sql.Types.INTEGER)
                stmt.setString(14, chapter.startFragmentId)
                stmt.setString(15, chapter.endFragmentId)
                stmt.setString(16, chapter.variable)
                stmt.setString(17, chapter.titleMD5)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }
    fun deleteByBookUrl(bookUrl: String) {
        val conn = Database.getConnection()
        conn.prepareStatement("DELETE FROM chapters WHERE bookUrl = ?").use { stmt ->
            stmt.setString(1, bookUrl)
            stmt.executeUpdate()
        }
    }
}

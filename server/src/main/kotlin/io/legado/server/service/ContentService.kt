package io.legado.server.service

import io.legado.server.db.dao.BookDao
import io.legado.server.db.dao.BookSourceDao
import io.legado.server.db.dao.ChapterDao
import io.legado.server.model.entity.Book
import io.legado.server.model.entity.BookChapter
import io.legado.server.model.entity.BookSource
import io.legado.server.webBook.WebBook
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

class ContentService(dataDir: File) {
    private val contentCacheDir = File(dataDir, "content_cache").also { it.mkdirs() }

    fun getContent(bookUrl: String, index: Int): String? {
        val book = BookDao.findByUrl(bookUrl) ?: return null
        val chapter = getChapterWithRetry(bookUrl, index) ?: return null

        val cached = getCachedContent(bookUrl, index)
        if (cached != null) {
            return cached
        }

        val bookSourceUrl = book.origin
        val bookSource = BookSourceDao.findByUrl(bookSourceUrl) ?: return null

        return runBlocking {
            try {
                val content = WebBook.getContentAwait(bookSource, book, chapter, chapter.url)
                cacheContent(bookUrl, index, content)
                content
            } catch (e: Exception) {
                null
            }
        }
    }

    fun refreshToc(bookUrl: String): List<BookChapter>? {
        val book = BookDao.findByUrl(bookUrl) ?: return null
        val bookSourceUrl = book.origin
        val bookSource = BookSourceDao.findByUrl(bookSourceUrl) ?: return null

        return runBlocking {
            try {
                if (book.tocUrl.isBlank()) {
                    WebBook.getBookInfoAwait(bookSource, book)
                }
                val result = WebBook.getChapterListAwait(bookSource, book)
                result.getOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getChapterWithRetry(bookUrl: String, index: Int): BookChapter? {
        return runBlocking {
            var chapter = ChapterDao.findByBookUrlAndIndex(bookUrl, index)
            var wait = 0
            while (chapter == null && wait < 30) {
                delay(1000)
                chapter = ChapterDao.findByBookUrlAndIndex(bookUrl, index)
                wait++
            }
            chapter
        }
    }

    private fun getCachedContent(bookUrl: String, index: Int): String? {
        val file = File(contentCacheDir, "${bookUrl.hashCode()}_$index")
        if (file.exists()) {
            return file.readText()
        }
        return null
    }

    private fun cacheContent(bookUrl: String, index: Int, content: String) {
        val file = File(contentCacheDir, "${bookUrl.hashCode()}_$index")
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}

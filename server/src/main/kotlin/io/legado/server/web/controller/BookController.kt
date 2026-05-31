package io.legado.server.web.controller

import com.google.gson.reflect.TypeToken
import io.legado.server.CacheManager
import io.legado.server.utils.GSON
import io.legado.server.db.dao.BookDao
import io.legado.server.db.dao.ChapterDao
import io.legado.server.image.ImageProxy
import io.legado.server.model.entity.Book
import io.legado.server.model.entity.BookProgress
import io.legado.server.service.ContentService
import io.legado.server.web.ReturnData
import io.legado.server.webBook.WebBook
import kotlinx.coroutines.runBlocking
import java.io.File

class BookController(dataDir: File) {
    private val contentService = ContentService(dataDir)
    private val imageProxy = ImageProxy(dataDir)

    fun getBookshelf(): ReturnData {
        val books = BookDao.findAll()
        return if (books.isEmpty()) {
            ReturnData.error("还没有添加小说")
        } else {
            val sorted = books.sortedByDescending { it.durChapterTime }
            ReturnData.success(sorted)
        }
    }

    fun getChapterList(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.firstOrNull()
        if (bookUrl.isNullOrEmpty()) {
            return ReturnData.error("参数url不能为空，请指定书籍地址")
        }
        val chapterList = ChapterDao.findByBookUrl(bookUrl)
        if (chapterList.isEmpty()) {
            return refreshToc(parameters)
        }
        return ReturnData.success(chapterList)
    }

    fun getBookContent(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.firstOrNull()
        val index = parameters["index"]?.firstOrNull()?.toIntOrNull()
        if (bookUrl.isNullOrEmpty()) {
            return ReturnData.error("参数url不能为空，请指定书籍地址")
        }
        if (index == null) {
            return ReturnData.error("参数index不能为空，请指定目录序号")
        }
        val content = contentService.getContent(bookUrl, index)
        if (content == null) {
            return ReturnData.error("未找到")
        }
        return ReturnData.success(content)
    }

    fun saveBook(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val book = GSON.fromJson(postData, Book::class.java)
            org.slf4j.LoggerFactory.getLogger("saveBook").info("Saving book: url=${book.bookUrl}, origin=${book.origin}, name=${book.name}")
            BookDao.save(book)
            ReturnData.success("")
        } catch (e: Exception) {
            org.slf4j.LoggerFactory.getLogger("saveBook").error("Error saving book: ${e.message}")
            ReturnData.error("格式不对: ${e.message}")
        }
    }

    fun deleteBook(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val book = GSON.fromJson(postData, Book::class.java)
            BookDao.delete(book.bookUrl)
            ReturnData.success("")
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }

    fun saveBookProgress(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val progress = GSON.fromJson(postData, BookProgress::class.java)
            val books = BookDao.findAll()
            val book = books.find { it.name == progress.name && it.author == progress.author }
            if (book != null) {
                book.durChapterIndex = progress.durChapterIndex
                book.durChapterPos = progress.durChapterPos
                book.durChapterTitle = progress.durChapterTitle
                book.durChapterTime = progress.durChapterTime
                BookDao.save(book)
                ReturnData.success("")
            } else {
                ReturnData.error("格式不对")
            }
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }

    fun getCover(parameters: Map<String, List<String>>): ReturnData {
        val coverPath = parameters["path"]?.firstOrNull()
        if (coverPath.isNullOrEmpty()) {
            return ReturnData.error("path为空")
        }
        return try {
            val bytes = imageProxy.getImage(coverPath, 84)
            ReturnData.success(bytes)
        } catch (e: Exception) {
            ReturnData.error(e.localizedMessage ?: "getCover error")
        }
    }

    fun getImg(parameters: Map<String, List<String>>): ReturnData {
        val url = parameters["url"]?.firstOrNull()
        val src = parameters["path"]?.firstOrNull()
        val width = parameters["width"]?.firstOrNull()?.toIntOrNull() ?: 640
        if (url.isNullOrEmpty()) return ReturnData.error("bookUrl为空")
        if (src.isNullOrEmpty()) return ReturnData.error("图片链接为空")
        return try {
            val bytes = imageProxy.getImage(src, width)
            ReturnData.success(bytes)
        } catch (e: Exception) {
            ReturnData.error(e.localizedMessage ?: "getImg error")
        }
    }

    fun refreshToc(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.firstOrNull()
        if (bookUrl.isNullOrEmpty()) {
            return ReturnData.error("参数url不能为空，请指定书籍地址")
        }
        return try {
            val chapterList = contentService.refreshToc(bookUrl)
            if (chapterList == null) {
                ReturnData.error("刷新目录失败，未找到书籍或书源")
            } else {
                val book = BookDao.findByUrl(bookUrl)
                if (book != null) {
                    ChapterDao.deleteByBookUrl(bookUrl)
                    ChapterDao.saveAll(chapterList)
                    BookDao.save(book)
                }
                ReturnData.success(chapterList)
            }
        } catch (e: Exception) {
            ReturnData.error(e.localizedMessage ?: "refresh toc error")
        }
    }

    fun getReadConfig(): ReturnData {
        val data = CacheManager.getFromMemory<String>("webReadConfig")
        if (data == null) return ReturnData.error("没有配置")
        return ReturnData.success(data)
    }

    fun saveReadConfig(postData: String?): ReturnData {
        if (postData != null) {
            CacheManager.putMemory("webReadConfig", postData)
        } else {
            CacheManager.remove("webReadConfig")
        }
        return ReturnData.success("")
    }
}

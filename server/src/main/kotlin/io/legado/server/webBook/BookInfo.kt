package io.legado.server.webBook

import io.legado.server.model.entity.Book
import io.legado.server.model.entity.BookSource
import io.legado.server.exception.NoStackTraceException
import io.legado.server.Debug
import io.legado.server.analyzeRule.AnalyzeRule
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setCoroutineContext
import io.legado.server.utils.NetworkUtils
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

object BookInfo {

    @Throws(Exception::class)
    suspend fun analyzeBookInfo(
        bookSource: BookSource,
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String?,
        canReName: Boolean,
    ) {
        body ?: throw NoStackTraceException("获取网页内容失败:$baseUrl")
        Debug.log(bookSource.bookSourceUrl, "≡获取成功:${baseUrl}")
        Debug.log(bookSource.bookSourceUrl, body, state = 20)
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        analyzeRule.setRedirectUrl(redirectUrl)
        analyzeRule.setCoroutineContext(coroutineContext)
        analyzeBookInfo(book, body, analyzeRule, bookSource, baseUrl, redirectUrl, canReName)
    }

    suspend fun analyzeBookInfo(
        book: Book,
        body: String,
        analyzeRule: AnalyzeRule,
        bookSource: BookSource,
        baseUrl: String,
        redirectUrl: String,
        canReName: Boolean,
    ) {
        val infoRule = bookSource.getBookInfoRule()
        infoRule.init?.let {
            if (it.isNotBlank()) {
                coroutineContext.ensureActive()
                Debug.log(bookSource.bookSourceUrl, "≡执行详情页初始化规则")
                analyzeRule.setContent(analyzeRule.getElement(it))
            }
        }
        val mCanReName = canReName && !infoRule.canReName.isNullOrBlank()
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取书名")
        formatBookName(analyzeRule.getString(infoRule.name)).let {
            if (it.isNotEmpty() && (mCanReName || book.name.isEmpty())) {
                book.name = it
            }
            Debug.log(bookSource.bookSourceUrl, "└${it}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取作者")
        formatBookAuthor(analyzeRule.getString(infoRule.author)).let {
            if (it.isNotEmpty() && (mCanReName || book.author.isEmpty())) {
                book.author = it
            }
            Debug.log(bookSource.bookSourceUrl, "└${it}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取分类")
        try {
            analyzeRule.getStringList(infoRule.kind)
                ?.joinToString(",")
                ?.let {
                    if (it.isNotEmpty()) book.kind = it
                    Debug.log(bookSource.bookSourceUrl, "└${it}")
                } ?: Debug.log(bookSource.bookSourceUrl, "└")
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取字数")
        try {
            wordCountFormat(analyzeRule.getString(infoRule.wordCount)).let {
                if (it.isNotEmpty()) book.wordCount = it
                Debug.log(bookSource.bookSourceUrl, "└${it}")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取最新章节")
        try {
            analyzeRule.getString(infoRule.lastChapter).let {
                if (it.isNotEmpty()) book.latestChapterTitle = it
                Debug.log(bookSource.bookSourceUrl, "└${it}")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取简介")
        try {
            htmlFormat(analyzeRule.getString(infoRule.intro)).let {
                if (it.isNotEmpty()) book.intro = it
                Debug.log(bookSource.bookSourceUrl, "└${it}")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取封面链接")
        try {
            analyzeRule.getString(infoRule.coverUrl).let {
                if (it.isNotEmpty()) {
                    book.coverUrl =
                        NetworkUtils.getAbsoluteURL(redirectUrl, it)
                }
                Debug.log(bookSource.bookSourceUrl, "└${it}")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}")
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取目录链接")
        book.tocUrl = analyzeRule.getString(infoRule.tocUrl, isUrl = true)
        if (book.tocUrl.isEmpty()) book.tocUrl = baseUrl
        if (book.tocUrl == baseUrl) {
            book.tocHtml = body
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.tocUrl}")
    }

    private fun formatBookName(name: String): String {
        return name.replace(Regex("\\s+作\\s+者.*|\\s+\\S+\\s+著"), "").trim()
    }

    private fun formatBookAuthor(author: String): String {
        return author.replace(Regex("^\\s*作\\s*者[:：\\s]+|\\s+著"), "").trim()
    }

    private fun htmlFormat(html: String?): String {
        html ?: return ""
        return org.jsoup.Jsoup.parse(html).wholeText()
    }

    private fun wordCountFormat(wc: String?): String {
        if (wc == null) return ""
        val trimmed = wc.trim()
        if (trimmed.isEmpty()) return ""
        val num = trimmed.toIntOrNull()
        return if (num != null) {
            if (num > 10000) {
                String.format("%.2f", num / 10000.0) + "万字"
            } else {
                "${num}字"
            }
        } else {
            trimmed
        }
    }
}

package io.legado.server.webBook

import io.legado.server.constant.AppPattern
import io.legado.server.model.entity.Book
import io.legado.server.model.entity.BookChapter
import io.legado.server.model.entity.BookSource
import io.legado.server.model.entity.rule.ContentRule
import io.legado.server.exception.ContentEmptyException
import io.legado.server.exception.NoStackTraceException
import io.legado.server.Debug
import io.legado.server.analyzeRule.AnalyzeRule
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setChapter
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setCoroutineContext
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setNextChapterUrl
import io.legado.server.analyzeRule.AnalyzeUrl
import io.legado.server.utils.NetworkUtils
import kotlinx.coroutines.ensureActive
import org.apache.commons.text.StringEscapeUtils
import kotlin.coroutines.coroutineContext

object BookContent {

    @Throws(Exception::class)
    suspend fun analyzeContent(
        bookSource: BookSource,
        book: Book,
        bookChapter: BookChapter,
        baseUrl: String,
        redirectUrl: String,
        body: String?,
        nextChapterUrl: String?,
        needSave: Boolean = true
    ): String {
        body ?: throw NoStackTraceException("获取网页内容失败:$baseUrl")
        Debug.log(bookSource.bookSourceUrl, "≡获取成功:${baseUrl}")
        Debug.log(bookSource.bookSourceUrl, body, state = 40)
        val contentList = arrayListOf<String>()
        val nextUrlList = arrayListOf(redirectUrl)
        val contentRule = bookSource.getContentRule()
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body, baseUrl)
        analyzeRule.setRedirectUrl(redirectUrl)
        analyzeRule.setCoroutineContext(coroutineContext)
        analyzeRule.setChapter(bookChapter)
        analyzeRule.setNextChapterUrl(nextChapterUrl)
        coroutineContext.ensureActive()
        val titleRule = contentRule.title
        if (!titleRule.isNullOrBlank()) {
            val title = analyzeRule.runCatching {
                getString(titleRule)
            }.onFailure {
                Debug.log(bookSource.bookSourceUrl, "获取标题出错, ${it.localizedMessage}")
            }.getOrNull()
            if (!title.isNullOrBlank()) {
                bookChapter.title = title
            }
        }
        var contentData = analyzeContent(
            book, baseUrl, redirectUrl, body, contentRule, bookChapter, bookSource, nextChapterUrl
        )
        contentList.add(contentData.first)
        if (contentData.second.size == 1) {
            var nextUrl = contentData.second[0]
            while (nextUrl.isNotEmpty() && !nextUrlList.contains(nextUrl)) {
                if (!nextChapterUrl.isNullOrEmpty()
                    && NetworkUtils.getAbsoluteURL(redirectUrl, nextUrl)
                    == NetworkUtils.getAbsoluteURL(redirectUrl, nextChapterUrl)
                ) break
                nextUrlList.add(nextUrl)
                coroutineContext.ensureActive()
                val analyzeUrl = AnalyzeUrl(
                    mUrl = nextUrl,
                    source = bookSource,
                    ruleData = book,
                    coroutineContext = coroutineContext
                )
                val res = analyzeUrl.getStrResponseAwait()
                res.body?.let { nextBody ->
                    contentData = analyzeContent(
                        book, nextUrl, res.url, nextBody, contentRule,
                        bookChapter, bookSource, nextChapterUrl,
                        printLog = false
                    )
                    nextUrl =
                        if (contentData.second.isNotEmpty()) contentData.second[0] else ""
                    contentList.add(contentData.first)
                    Debug.log(bookSource.bookSourceUrl, "第${contentList.size}页完成")
                }
            }
            Debug.log(bookSource.bookSourceUrl, "◇本章总页数:${nextUrlList.size}")
        } else if (contentData.second.size > 1) {
            Debug.log(bookSource.bookSourceUrl, "◇顺序解析正文,总页数:${contentData.second.size}")
            for (urlStr in contentData.second) {
                val analyzeUrl = AnalyzeUrl(
                    mUrl = urlStr,
                    source = bookSource,
                    ruleData = book,
                    coroutineContext = coroutineContext
                )
                val res = analyzeUrl.getStrResponseAwait()
                val result = analyzeContent(
                    book, urlStr, res.url, res.body!!, contentRule,
                    bookChapter, bookSource, nextChapterUrl,
                    getNextPageUrl = false,
                    printLog = false
                ).first
                coroutineContext.ensureActive()
                contentList.add(result)
            }
        }
        var contentStr = contentList.joinToString("\n")
        val replaceRegex = contentRule.replaceRegex
        if (!replaceRegex.isNullOrEmpty()) {
            contentStr = contentStr.split(AppPattern.rnRegex).joinToString("\n") { it.trim() }
            contentStr = analyzeRule.getString(replaceRegex, contentStr)
            contentStr = contentStr.split(AppPattern.rnRegex).joinToString("\n") { "　　$it" }
        }
        Debug.log(bookSource.bookSourceUrl, "┌获取章节名称")
        Debug.log(bookSource.bookSourceUrl, "└${bookChapter.title}")
        Debug.log(bookSource.bookSourceUrl, "┌获取正文内容")
        Debug.log(bookSource.bookSourceUrl, "└\n$contentStr")
        if (!bookChapter.isVolume && contentStr.isBlank()) {
            throw ContentEmptyException("内容为空")
        }
        return contentStr
    }

    @Throws(Exception::class)
    private suspend fun analyzeContent(
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String,
        contentRule: ContentRule,
        chapter: BookChapter,
        bookSource: BookSource,
        nextChapterUrl: String?,
        getNextPageUrl: Boolean = true,
        printLog: Boolean = true
    ): Pair<String, List<String>> {
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body, baseUrl)
        analyzeRule.setCoroutineContext(coroutineContext)
        val rUrl = analyzeRule.setRedirectUrl(redirectUrl)
        analyzeRule.setNextChapterUrl(nextChapterUrl)
        val nextUrlList = arrayListOf<String>()
        analyzeRule.setChapter(chapter)
        var content = analyzeRule.getString(contentRule.content, unescape = false)
        content = formatKeepImg(content, rUrl?.toString() ?: redirectUrl)
        if (content.indexOf('&') > -1) {
            content = StringEscapeUtils.unescapeHtml4(content)
        }
        if (getNextPageUrl) {
            val nextUrlRule = contentRule.nextContentUrl
            if (!nextUrlRule.isNullOrEmpty()) {
                Debug.log(bookSource.bookSourceUrl, "┌获取正文下一页链接", printLog)
                analyzeRule.getStringList(nextUrlRule, isUrl = true)?.let {
                    nextUrlList.addAll(it)
                }
                Debug.log(bookSource.bookSourceUrl, "└" + nextUrlList.joinToString("，"), printLog)
            }
        }
        return Pair(content, nextUrlList)
    }

    private fun formatKeepImg(html: String?, redirectUrl: String? = null): String {
        html ?: return ""
        val nbspRegex = Regex("(&nbsp;)+")
        val espRegex = Regex("(&ensp;|&emsp;)")
        val noPrintRegex = Regex("(&thinsp;|&zwnj;|&zwj;|\u2009|\u200C|\u200D)")
        val wrapHtmlRegex = Regex("</?(?:div|p|br|hr|h\\d|article|dd|dl)[^>]*>")
        val commentRegex = Regex("<!--[^>]*-->")
        val notImgHtmlRegex = Regex("</?(?!img)[a-zA-Z]+(?=[ >])[^<>]*>")
        val indent1Regex = Regex("\\s*\\n+\\s*")
        val indent2Regex = Regex("^[\\n\\s]+")
        val lastRegex = Regex("[\\n\\s]+$")
        val imgPattern = Regex(
            "<img[^>]*\\s+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>|<img[^>]*\\s+data-src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            RegexOption.IGNORE_CASE
        )
        var result = html
            .replace(nbspRegex, " ")
            .replace(espRegex, " ")
            .replace(noPrintRegex, "")
            .replace(wrapHtmlRegex, "\n")
            .replace(commentRegex, "")
            .replace(notImgHtmlRegex, "")
        result = result.replace(imgPattern) { match ->
            val src = match.groupValues[1].ifEmpty { match.groupValues[2] }
            val absSrc = if (src.isNotEmpty() && redirectUrl != null) {
                NetworkUtils.getAbsoluteURL(redirectUrl, src) ?: src
            } else src
            "<img src=\"$absSrc\">"
        }
        result = result
            .replace(indent1Regex, "\n　　")
            .replace(indent2Regex, "　　")
            .replace(lastRegex, "")
        return result
    }
}

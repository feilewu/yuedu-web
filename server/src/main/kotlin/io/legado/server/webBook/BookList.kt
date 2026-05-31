package io.legado.server.webBook

import io.legado.server.model.entity.Book
import io.legado.server.model.entity.BookSource
import io.legado.server.model.entity.SearchBook
import io.legado.server.model.entity.rule.BookListRule
import io.legado.server.exception.NoStackTraceException
import io.legado.server.Debug
import io.legado.server.analyzeRule.AnalyzeRule
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setCoroutineContext
import io.legado.server.analyzeRule.AnalyzeRule.Companion.setRuleData
import io.legado.server.analyzeRule.AnalyzeUrl
import io.legado.server.analyzeRule.RuleData
import io.legado.server.utils.NetworkUtils
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

object BookList {

    @Throws(Exception::class)
    suspend fun analyzeBookList(
        bookSource: BookSource,
        ruleData: RuleData,
        analyzeUrl: AnalyzeUrl,
        baseUrl: String,
        body: String?,
        isSearch: Boolean = true,
        isRedirect: Boolean = false,
        filter: ((name: String, author: String) -> Boolean)? = null,
        shouldBreak: ((size: Int) -> Boolean)? = null
    ): ArrayList<SearchBook> {
        body ?: throw NoStackTraceException("获取网页内容失败:${analyzeUrl.ruleUrl}")
        val bookList = ArrayList<SearchBook>()
        Debug.log(bookSource.bookSourceUrl, "≡获取成功:${analyzeUrl.ruleUrl}")
        Debug.log(bookSource.bookSourceUrl, body, state = 10)
        val analyzeRule = AnalyzeRule(ruleData, bookSource)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        analyzeRule.setRedirectUrl(baseUrl)
        analyzeRule.setCoroutineContext(coroutineContext)
        if (isSearch) bookSource.bookUrlPattern?.let {
            coroutineContext.ensureActive()
            if (baseUrl.matches(it.toRegex())) {
                Debug.log(bookSource.bookSourceUrl, "≡链接为详情页")
                getInfoItem(
                    bookSource, analyzeRule, analyzeUrl, body, baseUrl,
                    ruleData.getVariable(), isRedirect, filter
                )?.let { searchBook ->
                    searchBook.infoHtml = body
                    bookList.add(searchBook)
                }
                return bookList
            }
        }
        val collections: List<Any>
        var reverse = false
        val bookListRule: BookListRule = when {
            isSearch -> bookSource.getSearchRule()
            bookSource.getExploreRule().bookList.isNullOrBlank() -> bookSource.getSearchRule()
            else -> bookSource.getExploreRule()
        }
        var ruleList: String = bookListRule.bookList ?: ""
        if (ruleList.startsWith("-")) {
            reverse = true
            ruleList = ruleList.substring(1)
        }
        if (ruleList.startsWith("+")) {
            ruleList = ruleList.substring(1)
        }
        Debug.log(bookSource.bookSourceUrl, "┌获取书籍列表")
        collections = analyzeRule.getElements(ruleList)
        coroutineContext.ensureActive()
        if (collections.isEmpty() && bookSource.bookUrlPattern.isNullOrEmpty()) {
            Debug.log(bookSource.bookSourceUrl, "└列表为空,按详情页解析")
            getInfoItem(
                bookSource, analyzeRule, analyzeUrl, body, baseUrl, ruleData.getVariable(),
                isRedirect, filter
            )?.let { searchBook ->
                searchBook.infoHtml = body
                bookList.add(searchBook)
            }
        } else {
            val ruleName = analyzeRule.splitSourceRule(bookListRule.name)
            val ruleBookUrl = analyzeRule.splitSourceRule(bookListRule.bookUrl)
            val ruleAuthor = analyzeRule.splitSourceRule(bookListRule.author)
            val ruleCoverUrl = analyzeRule.splitSourceRule(bookListRule.coverUrl)
            val ruleIntro = analyzeRule.splitSourceRule(bookListRule.intro)
            val ruleKind = analyzeRule.splitSourceRule(bookListRule.kind)
            val ruleLastChapter = analyzeRule.splitSourceRule(bookListRule.lastChapter)
            val ruleWordCount = analyzeRule.splitSourceRule(bookListRule.wordCount)
            Debug.log(bookSource.bookSourceUrl, "└列表大小:${collections.size}")
            for ((index, item) in collections.withIndex()) {
                getSearchItem(
                    bookSource, analyzeRule, item, baseUrl, ruleData.getVariable(),
                    index == 0, filter,
                    ruleName, ruleBookUrl, ruleAuthor, ruleCoverUrl,
                    ruleIntro, ruleKind, ruleLastChapter, ruleWordCount
                )?.let { searchBook ->
                    if (baseUrl == searchBook.bookUrl) {
                        searchBook.infoHtml = body
                    }
                    bookList.add(searchBook)
                }
                if (shouldBreak?.invoke(bookList.size) == true) break
            }
            val lh = LinkedHashSet(bookList)
            bookList.clear()
            bookList.addAll(lh)
            if (reverse) bookList.reverse()
        }
        Debug.log(bookSource.bookSourceUrl, "◇书籍总数:${bookList.size}")
        return bookList
    }

    @Throws(Exception::class)
    private suspend fun getInfoItem(
        bookSource: BookSource,
        analyzeRule: AnalyzeRule,
        analyzeUrl: AnalyzeUrl,
        body: String,
        baseUrl: String,
        variable: String?,
        isRedirect: Boolean,
        filter: ((name: String, author: String) -> Boolean)?
    ): SearchBook? {
        val book = Book(variable = variable)
        book.bookUrl = if (isRedirect) {
            baseUrl
        } else {
            NetworkUtils.getAbsoluteURL(analyzeUrl.url, analyzeUrl.ruleUrl) ?: analyzeUrl.ruleUrl
        }
        book.origin = bookSource.bookSourceUrl
        book.originName = bookSource.bookSourceName
        book.originOrder = bookSource.customOrder
        book.type = bookSource.bookSourceType
        analyzeRule.setRuleData(book)
        BookInfo.analyzeBookInfo(book, body, analyzeRule, bookSource, baseUrl, baseUrl, false)
        if (filter?.invoke(book.name, book.author) == false) return null
        return if (book.name.isNotBlank()) book.toSearchBook() else null
    }

    @Throws(Exception::class)
    private suspend fun getSearchItem(
        bookSource: BookSource,
        analyzeRule: AnalyzeRule,
        item: Any,
        baseUrl: String,
        variable: String?,
        log: Boolean,
        filter: ((name: String, author: String) -> Boolean)?,
        ruleName: List<AnalyzeRule.SourceRule>,
        ruleBookUrl: List<AnalyzeRule.SourceRule>,
        ruleAuthor: List<AnalyzeRule.SourceRule>,
        ruleCoverUrl: List<AnalyzeRule.SourceRule>,
        ruleIntro: List<AnalyzeRule.SourceRule>,
        ruleKind: List<AnalyzeRule.SourceRule>,
        ruleLastChapter: List<AnalyzeRule.SourceRule>,
        ruleWordCount: List<AnalyzeRule.SourceRule>
    ): SearchBook? {
        val searchBook = SearchBook(variable = variable)
        searchBook.type = bookSource.bookSourceType
        searchBook.origin = bookSource.bookSourceUrl
        searchBook.originName = bookSource.bookSourceName
        searchBook.originOrder = bookSource.customOrder
        analyzeRule.setRuleData(searchBook)
        analyzeRule.setContent(item)
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取书名", log)
        searchBook.name = formatBookName(analyzeRule.getString(ruleName))
        Debug.log(bookSource.bookSourceUrl, "└${searchBook.name}", log)
        if (searchBook.name.isEmpty()) return null
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取作者", log)
        searchBook.author = formatBookAuthor(analyzeRule.getString(ruleAuthor))
        Debug.log(bookSource.bookSourceUrl, "└${searchBook.author}", log)
        if (filter?.invoke(searchBook.name, searchBook.author) == false) return null
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取分类", log)
        try {
            searchBook.kind = analyzeRule.getStringList(ruleKind)?.joinToString(",")
            Debug.log(bookSource.bookSourceUrl, "└${searchBook.kind ?: ""}", log)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}", log)
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取字数", log)
        try {
            searchBook.wordCount = wordCountFormat(analyzeRule.getString(ruleWordCount))
            Debug.log(bookSource.bookSourceUrl, "└${searchBook.wordCount}", log)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}", log)
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取最新章节", log)
        try {
            searchBook.latestChapterTitle = analyzeRule.getString(ruleLastChapter)
            Debug.log(bookSource.bookSourceUrl, "└${searchBook.latestChapterTitle}", log)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}", log)
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取简介", log)
        try {
            searchBook.intro = htmlFormat(analyzeRule.getString(ruleIntro))
            Debug.log(bookSource.bookSourceUrl, "└${searchBook.intro}", log)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}", log)
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取封面链接", log)
        try {
            analyzeRule.getString(ruleCoverUrl).let {
                if (it.isNotEmpty()) {
                    searchBook.coverUrl = NetworkUtils.getAbsoluteURL(baseUrl, it)
                }
            }
            Debug.log(bookSource.bookSourceUrl, "└${searchBook.coverUrl ?: ""}", log)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Debug.log(bookSource.bookSourceUrl, "└${e.localizedMessage}", log)
        }
        coroutineContext.ensureActive()
        Debug.log(bookSource.bookSourceUrl, "┌获取详情页链接", log)
        searchBook.bookUrl = analyzeRule.getString(ruleBookUrl, isUrl = true)
        if (searchBook.bookUrl.isEmpty()) {
            searchBook.bookUrl = baseUrl
        }
        Debug.log(bookSource.bookSourceUrl, "└${searchBook.bookUrl}", log)
        return searchBook
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

package io.legado.server.model.entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
data class SearchBook(
    override var bookUrl: String = "",
    var origin: String = "",
    var originName: String = "",
    var type: Int = 0b1000,
    override var name: String = "",
    override var author: String = "",
    override var kind: String? = null,
    var coverUrl: String? = null,
    var intro: String? = null,
    override var wordCount: String? = null,
    var latestChapterTitle: String? = null,
    var tocUrl: String = "",
    var time: Long = System.currentTimeMillis(),
    override var variable: String? = null,
    var originOrder: Int = 0,
    var chapterWordCountText: String? = null,
    var chapterWordCount: Int = -1,
    var respondTime: Int = -1
) : BaseBook, Comparable<SearchBook> {
    override var infoHtml: String? = null
    override var tocHtml: String? = null
    override fun equals(other: Any?) = other is SearchBook && other.bookUrl == bookUrl
    override fun hashCode() = bookUrl.hashCode()
    override fun compareTo(other: SearchBook): Int {
        return other.originOrder - this.originOrder
    }
    override val variableMap: HashMap<String, String> by lazy {
        try {
            Gson().fromJson(variable, object : TypeToken<HashMap<String, String>>() {}.type)
                as? HashMap<String, String> ?: HashMap()
        } catch (_: Exception) {
            HashMap()
        }
    }
    val origins: LinkedHashSet<String> by lazy { linkedSetOf(origin) }
    fun addOrigin(origin: String) {
        origins.add(origin)
    }
    fun getDisplayLastChapterTitle(): String {
        latestChapterTitle?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return "无最新章节"
    }
    fun releaseHtmlData() {
        infoHtml = null
        tocHtml = null
    }
    fun primaryStr(): String {
        return origin + bookUrl
    }
    private val allBookTypeLocal = 0b1000 or 0b100000 or 0b1000000 or 0b10000000 or 0b100000000
    fun sameBookTypeLocal(bookType: Int): Boolean {
        return type and allBookTypeLocal == bookType and allBookTypeLocal
    }
    fun toBook() = Book(
        name = name,
        author = author,
        kind = kind,
        bookUrl = bookUrl,
        origin = origin,
        originName = originName,
        type = type,
        wordCount = wordCount,
        latestChapterTitle = latestChapterTitle,
        coverUrl = coverUrl,
        intro = intro,
        tocUrl = tocUrl,
        originOrder = originOrder,
        variable = variable
    ).apply {
        this.infoHtml = this@SearchBook.infoHtml
        this.tocHtml = this@SearchBook.tocHtml
    }
}

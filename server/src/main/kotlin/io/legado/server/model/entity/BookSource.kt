package io.legado.server.model.entity
import io.legado.server.model.entity.rule.BookInfoRule
import io.legado.server.model.entity.rule.ContentRule
import io.legado.server.model.entity.rule.ExploreRule
import io.legado.server.model.entity.rule.ReviewRule
import io.legado.server.model.entity.rule.SearchRule
import io.legado.server.model.entity.rule.TocRule
@Suppress("unused")
data class BookSource(
    var bookSourceUrl: String = "",
    var bookSourceName: String = "",
    var bookSourceGroup: String? = null,
    var bookSourceType: Int = 0,
    var bookUrlPattern: String? = null,
    var customOrder: Int = 0,
    var enabled: Boolean = true,
    var enabledExplore: Boolean = true,
    override var jsLib: String? = null,
    override var enabledCookieJar: Boolean? = true,
    override var concurrentRate: String? = null,
    override var header: String? = null,
    override var loginUrl: String? = null,
    override var loginUi: String? = null,
    var loginCheckJs: String? = null,
    var coverDecodeJs: String? = null,
    var bookSourceComment: String? = null,
    var variableComment: String? = null,
    var lastUpdateTime: Long = 0,
    var respondTime: Long = 180000L,
    var weight: Int = 0,
    var exploreUrl: String? = null,
    var exploreScreen: String? = null,
    var ruleExplore: ExploreRule? = null,
    var searchUrl: String? = null,
    var ruleSearch: SearchRule? = null,
    var ruleBookInfo: BookInfoRule? = null,
    var ruleToc: TocRule? = null,
    var ruleContent: ContentRule? = null,
    var eventListener: Boolean = false,
    var customButton: Boolean = false,
    var ruleReview: ReviewRule? = null
) : BaseSource {
    override fun getTag(): String {
        return bookSourceName
    }
    override fun getKey(): String {
        return bookSourceUrl
    }
    override fun getSource(): BaseSource? = this
    override fun hashCode(): Int {
        return bookSourceUrl.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return if (other is BookSource) other.bookSourceUrl == bookSourceUrl else false
    }
    fun getSearchRule(): SearchRule {
        ruleSearch?.let { return it }
        val rule = SearchRule()
        ruleSearch = rule
        return rule
    }
    fun getExploreRule(): ExploreRule {
        ruleExplore?.let { return it }
        val rule = ExploreRule()
        ruleExplore = rule
        return rule
    }
    fun getBookInfoRule(): BookInfoRule {
        ruleBookInfo?.let { return it }
        val rule = BookInfoRule()
        ruleBookInfo = rule
        return rule
    }
    fun getTocRule(): TocRule {
        ruleToc?.let { return it }
        val rule = TocRule()
        ruleToc = rule
        return rule
    }
    fun getContentRule(): ContentRule {
        ruleContent?.let { return it }
        val rule = ContentRule()
        ruleContent = rule
        return rule
    }
    fun getDisPlayNameGroup(): String {
        return if (bookSourceGroup.isNullOrBlank()) {
            bookSourceName
        } else {
            String.format("%s (%s)", bookSourceName, bookSourceGroup)
        }
    }
    fun addGroup(groups: String): BookSource {
        val existing = bookSourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: hashSetOf()
        existing.addAll(groups.split(",", ";", "，", "；").filter { it.isNotBlank() })
        bookSourceGroup = existing.joinToString(",")
        return this
    }
    fun removeGroup(groups: String): BookSource {
        val existing = bookSourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: hashSetOf()
        existing.removeAll(groups.split(",", ";", "，", "；").filter { it.isNotBlank() }.toSet())
        bookSourceGroup = existing.joinToString(",")
        return this
    }
    fun hasGroup(group: String): Boolean {
        val groups = bookSourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: return false
        return group in groups
    }
    fun removeInvalidGroups() {
        removeGroup(getInvalidGroupNames())
    }
    fun removeErrorComment() {
        bookSourceComment = bookSourceComment
            ?.split("\n\n")
            ?.filterNot {
                it.startsWith("// Error: ")
            }?.joinToString("\n")
    }
    fun addErrorComment(e: Throwable) {
        bookSourceComment =
            "// Error: ${e.localizedMessage}" + if (bookSourceComment.isNullOrBlank())
                "" else "\n\n${bookSourceComment}"
    }
    fun getCheckKeyword(default: String): String {
        ruleSearch?.checkKeyWord?.let {
            if (it.isNotBlank()) {
                return it
            }
        }
        return default
    }
    fun getInvalidGroupNames(): String {
        val groups = bookSourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: return ""
        return groups.filter { "失效" in it || it == "校验超时" }.joinToString()
    }
    fun getDisplayVariableComment(otherComment: String): String {
        return if (variableComment.isNullOrBlank()) {
            otherComment
        } else {
            "${variableComment}\n$otherComment"
        }
    }
    fun equal(source: BookSource): Boolean {
        return equal(bookSourceName, source.bookSourceName)
                && equal(bookSourceUrl, source.bookSourceUrl)
                && equal(bookSourceGroup, source.bookSourceGroup)
                && bookSourceType == source.bookSourceType
                && equal(bookUrlPattern, source.bookUrlPattern)
                && equal(bookSourceComment, source.bookSourceComment)
                && customOrder == source.customOrder
                && enabled == source.enabled
                && enabledExplore == source.enabledExplore
                && enabledCookieJar == source.enabledCookieJar
                && equal(variableComment, source.variableComment)
                && equal(concurrentRate, source.concurrentRate)
                && equal(jsLib, source.jsLib)
                && equal(header, source.header)
                && equal(loginUrl, source.loginUrl)
                && equal(loginUi, source.loginUi)
                && equal(loginCheckJs, source.loginCheckJs)
                && equal(coverDecodeJs, source.coverDecodeJs)
                && equal(exploreUrl, source.exploreUrl)
                && equal(searchUrl, source.searchUrl)
                && getSearchRule() == source.getSearchRule()
                && getExploreRule() == source.getExploreRule()
                && getBookInfoRule() == source.getBookInfoRule()
                && getTocRule() == source.getTocRule()
                && getContentRule() == source.getContentRule()
    }
    private fun equal(a: String?, b: String?) = a == b || (a.isNullOrEmpty() && b.isNullOrEmpty())
}

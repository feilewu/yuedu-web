package io.legado.server.model.entity
@Suppress("unused")
data class RssSource(
    var sourceUrl: String = "",
    var sourceName: String = "",
    var sourceIcon: String = "",
    var sourceGroup: String? = null,
    var sourceComment: String? = null,
    var enabled: Boolean = true,
    var variableComment: String? = null,
    override var jsLib: String? = null,
    override var enabledCookieJar: Boolean? = true,
    override var concurrentRate: String? = null,
    override var header: String? = null,
    override var loginUrl: String? = null,
    override var loginUi: String? = null,
    var loginCheckJs: String? = null,
    var coverDecodeJs: String? = null,
    var sortUrl: String? = null,
    var singleUrl: Boolean = false,
    var articleStyle: Int = 0,
    var ruleArticles: String? = null,
    var ruleNextPage: String? = null,
    var ruleTitle: String? = null,
    var rulePubDate: String? = null,
    var ruleDescription: String? = null,
    var ruleImage: String? = null,
    var ruleLink: String? = null,
    var ruleContent: String? = null,
    var contentWhitelist: String? = null,
    var contentBlacklist: String? = null,
    var shouldOverrideUrlLoading: String? = null,
    var style: String? = null,
    var enableJs: Boolean = true,
    var loadWithBaseUrl: Boolean = true,
    var injectJs: String? = null,
    var lastUpdateTime: Long = 0,
    var customOrder: Int = 0
) : BaseSource {
    override fun getTag(): String {
        return sourceName
    }
    override fun getKey(): String {
        return sourceUrl
    }
    override fun getSource(): BaseSource? = this
    override fun equals(other: Any?): Boolean {
        if (other is RssSource) {
            return other.sourceUrl == sourceUrl
        }
        return false
    }
    override fun hashCode() = sourceUrl.hashCode()
    fun equal(source: RssSource): Boolean {
        return equal(sourceUrl, source.sourceUrl)
                && equal(sourceName, source.sourceName)
                && equal(sourceIcon, source.sourceIcon)
                && enabled == source.enabled
                && equal(sourceGroup, source.sourceGroup)
                && enabledCookieJar == source.enabledCookieJar
                && equal(sourceComment, source.sourceComment)
                && equal(concurrentRate, source.concurrentRate)
                && equal(header, source.header)
                && equal(loginUrl, source.loginUrl)
                && equal(loginUi, source.loginUi)
                && equal(loginCheckJs, source.loginCheckJs)
                && equal(coverDecodeJs, source.coverDecodeJs)
                && equal(sortUrl, source.sortUrl)
                && singleUrl == source.singleUrl
                && articleStyle == source.articleStyle
                && equal(ruleArticles, source.ruleArticles)
                && equal(ruleNextPage, source.ruleNextPage)
                && equal(ruleTitle, source.ruleTitle)
                && equal(rulePubDate, source.rulePubDate)
                && equal(ruleDescription, source.ruleDescription)
                && equal(ruleLink, source.ruleLink)
                && equal(ruleContent, source.ruleContent)
                && enableJs == source.enableJs
                && loadWithBaseUrl == source.loadWithBaseUrl
                && equal(variableComment, source.variableComment)
                && equal(style, source.style)
                && equal(injectJs, source.injectJs)
    }
    private fun equal(a: String?, b: String?): Boolean {
        return a == b || (a.isNullOrEmpty() && b.isNullOrEmpty())
    }
    fun getDisplayNameGroup(): String {
        return if (sourceGroup.isNullOrBlank()) {
            sourceName
        } else {
            String.format("%s (%s)", sourceName, sourceGroup)
        }
    }
    fun addGroup(groups: String): RssSource {
        val existing = sourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: hashSetOf()
        existing.addAll(groups.split(",", ";", "，", "；").filter { it.isNotBlank() })
        sourceGroup = existing.joinToString(",")
        return this
    }
    fun removeGroup(groups: String): RssSource {
        val existing = sourceGroup?.split(",", ";", "，", "；")
            ?.filter { it.isNotBlank() }?.toHashSet() ?: hashSetOf()
        existing.removeAll(groups.split(",", ";", "，", "；").filter { it.isNotBlank() }.toSet())
        sourceGroup = existing.joinToString(",")
        return this
    }
    fun getDisplayVariableComment(otherComment: String): String {
        return if (variableComment.isNullOrBlank()) {
            otherComment
        } else {
            "${variableComment}\n$otherComment"
        }
    }
}

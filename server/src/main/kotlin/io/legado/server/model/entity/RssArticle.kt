package io.legado.server.model.entity

data class RssArticle(
    var articleUrl: String = "",
    var title: String = "",
    var pubDate: String = "",
    var description: String = "",
    var image: String = "",
    var link: String = "",
    var content: String = "",
    var origin: String = "",
    var originName: String = "",
    var sourceUrl: String = "",
    var isRead: Boolean = false,
    var isFavorite: Boolean = false,
    var variable: String? = null
) : RuleDataInterface {
    override val variableMap: HashMap<String, String> by lazy { hashMapOf() }
    override fun equals(other: Any?): Boolean {
        if (other is RssArticle) return other.articleUrl == articleUrl
        return false
    }
    override fun hashCode() = articleUrl.hashCode()
}

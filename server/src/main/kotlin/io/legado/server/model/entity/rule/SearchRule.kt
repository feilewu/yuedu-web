package io.legado.server.model.entity.rule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
/**
 * 搜索结果处理规则
 */
data class SearchRule(
    var checkKeyWord: String? = null,
    override var bookList: String? = null,
    override var name: String? = null,
    override var author: String? = null,
    override var intro: String? = null,
    override var kind: String? = null,
    override var lastChapter: String? = null,
    override var updateTime: String? = null,
    override var bookUrl: String? = null,
    override var coverUrl: String? = null,
    override var wordCount: String? = null
) : BookListRule {
    companion object {
        val jsonDeserializer = JsonDeserializer<SearchRule?> { json, _, _ ->
            when {
                json.isJsonObject -> Gson().fromJson(json, SearchRule::class.java)
                json.isJsonPrimitive -> Gson().fromJson(json.asString, SearchRule::class.java)
                else -> null
            }
        }
    }
}

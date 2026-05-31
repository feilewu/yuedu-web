package io.legado.server.model.entity.rule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
/**
 * 书籍详情页规则
 */
data class BookInfoRule(
    var init: String? = null,
    var name: String? = null,
    var author: String? = null,
    var intro: String? = null,
    var kind: String? = null,
    var lastChapter: String? = null,
    var updateTime: String? = null,
    var coverUrl: String? = null,
    var tocUrl: String? = null,
    var wordCount: String? = null,
    var canReName: String? = null,
    var downloadUrls: String? = null
) {
    companion object {
        val jsonDeserializer = JsonDeserializer<BookInfoRule?> { json, _, _ ->
            when {
                json.isJsonObject -> Gson().fromJson(json, BookInfoRule::class.java)
                json.isJsonPrimitive -> Gson().fromJson(json.asString, BookInfoRule::class.java)
                else -> null
            }
        }
    }
}

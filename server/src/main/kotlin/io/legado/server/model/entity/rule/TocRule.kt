package io.legado.server.model.entity.rule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
data class TocRule(
    var preUpdateJs: String? = null,
    var chapterList: String? = null,
    var chapterName: String? = null,
    var chapterUrl: String? = null,
    var formatJs: String? = null,
    var isVolume: String? = null,
    var isVip: String? = null,
    var isPay: String? = null,
    var updateTime: String? = null,
    var nextTocUrl: String? = null
) {
    companion object {
        val jsonDeserializer = JsonDeserializer<TocRule?> { json, _, _ ->
            when {
                json.isJsonObject -> Gson().fromJson(json, TocRule::class.java)
                json.isJsonPrimitive -> Gson().fromJson(json.asString, TocRule::class.java)
                else -> null
            }
        }
    }
}

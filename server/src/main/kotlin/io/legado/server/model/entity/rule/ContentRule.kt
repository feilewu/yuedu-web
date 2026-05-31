package io.legado.server.model.entity.rule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
/**
 * 正文处理规则
 */
data class ContentRule(
    var content: String? = null,
    var title: String? = null,
    var nextContentUrl: String? = null,
    var webJs: String? = null,
    var sourceRegex: String? = null,
    var replaceRegex: String? = null,
    var imageStyle: String? = null,
    var imageDecode: String? = null,
    var payAction: String? = null,
    var callBackJs: String? = null
) {
    companion object {
        val jsonDeserializer = JsonDeserializer<ContentRule?> { json, _, _ ->
            when {
                json.isJsonObject -> Gson().fromJson(json, ContentRule::class.java)
                json.isJsonPrimitive -> Gson().fromJson(json.asString, ContentRule::class.java)
                else -> null
            }
        }
    }
}

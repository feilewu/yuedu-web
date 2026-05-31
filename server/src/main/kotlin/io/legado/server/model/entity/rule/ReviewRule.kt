package io.legado.server.model.entity.rule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
data class ReviewRule(
    var enabled: Boolean = false,
    var reviewSummaryUrl: String? = null,
    var summaryListRule: String? = null,
    var summaryParagraphIndexRule: String? = null,
    var summaryParagraphDataRule: String? = null,
    var summaryCountRule: String? = null,
    var reviewDetailUrl: String? = null,
    var reviewDetailNextPageUrl: String? = null,
    var detailListRule: String? = null,
    var detailIdRule: String? = null,
    var detailAvatarRule: String? = null,
    var detailNameRule: String? = null,
    var detailBadgeRule: String? = null,
    var detailContentRule: String? = null,
    var replyListRule: String? = null,
    var replyIdRule: String? = null,
    var replyAvatarRule: String? = null,
    var replyNameRule: String? = null,
    var replyBadgeRule: String? = null,
    var replyContentRule: String? = null,
) {
    companion object {
        val jsonDeserializer = JsonDeserializer<ReviewRule?> { json, _, _ ->
            when {
                json.isJsonObject -> Gson().fromJson(json, ReviewRule::class.java)
                json.isJsonPrimitive -> Gson().fromJson(json.asString, ReviewRule::class.java)
                else -> null
            }
        }
    }
}

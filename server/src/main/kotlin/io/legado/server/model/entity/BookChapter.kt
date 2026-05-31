package io.legado.server.model.entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
data class BookChapter(
    var url: String = "",
    var title: String = "",
    var isVolume: Boolean = false,
    var baseUrl: String = "",
    var bookUrl: String = "",
    var index: Int = 0,
    var isVip: Boolean = false,
    var isPay: Boolean = false,
    var resourceUrl: String? = null,
    var tag: String? = null,
    var wordCount: String? = null,
    var start: Long? = null,
    var end: Long? = null,
    var startFragmentId: String? = null,
    var endFragmentId: String? = null,
    var variable: String? = null
) : RuleDataInterface {
    override val variableMap: HashMap<String, String> by lazy {
        try {
            Gson().fromJson(variable, object : TypeToken<HashMap<String, String>>() {}.type)
                as? HashMap<String, String> ?: hashMapOf()
        } catch (_: Exception) {
            hashMapOf()
        }
    }
    var titleMD5: String? = null
    override fun putVariable(key: String, value: String?): Boolean {
        if (super.putVariable(key, value)) {
            variable = Gson().toJson(variableMap)
        }
        return true
    }
    override fun hashCode() = url.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other is BookChapter) {
            return other.url == url
        }
        return false
    }
    fun primaryStr(): String {
        return bookUrl + url
    }
}

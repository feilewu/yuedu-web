package io.legado.server.model.entity
import com.google.gson.Gson
interface BaseRssArticle : RuleDataInterface {
    var origin: String
    var link: String
    var variable: String?
    override fun putVariable(key: String, value: String?): Boolean {
        if (super.putVariable(key, value)) {
            variable = Gson().toJson(variableMap)
        }
        return true
    }
}

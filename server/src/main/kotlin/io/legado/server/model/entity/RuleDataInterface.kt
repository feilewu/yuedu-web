package io.legado.server.model.entity
interface RuleDataInterface {
    val variableMap: HashMap<String, String>
    fun putVariable(key: String, value: String?): Boolean {
        val keyExist = variableMap.contains(key)
        return when {
            value == null -> {
                variableMap.remove(key)
                keyExist
            }
            value.length < 10000 -> {
                variableMap[key] = value
                true
            }
            else -> {
                variableMap.remove(key)
                keyExist
            }
        }
    }
    fun getVariable(key: String): String {
        return variableMap[key] ?: ""
    }

    fun putBigVariable(key: String, value: String?) {}
    fun getBigVariable(key: String): String? = null
}

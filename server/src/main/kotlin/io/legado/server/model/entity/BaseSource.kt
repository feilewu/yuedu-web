package io.legado.server.model.entity

import io.legado.server.constant.AppConst
import io.legado.server.constant.AppLog
import io.legado.server.constant.AppPattern
import io.legado.server.utils.GSON
import io.legado.server.utils.GSONStrict
import io.legado.server.utils.fromJsonObject
import io.legado.server.config.AppConfig

interface BaseSource {
    var concurrentRate: String?
    var loginUrl: String?
    var loginUi: String?
    var header: String?
    var enabledCookieJar: Boolean?
    var jsLib: String?

    fun getTag(): String
    fun getKey(): String
    fun getSource(): BaseSource?

    fun put(key: String, value: String): String = value

    fun get(key: String): String? = null

    fun getHeaderMap(hasLoginHeader: Boolean = false): HashMap<String, String> {
        val map = HashMap<String, String>()
        header?.let {
            try {
                GSONStrict.fromJsonObject<Map<String, String>>(it).getOrNull()?.let { m -> map.putAll(m) }
            } catch (_: Exception) {
                GSON.fromJsonObject<Map<String, String>>(it).getOrNull()?.let { m ->
                    map.putAll(m)
                }
            }
        }
        if (!map.containsKey(AppConst.UA_NAME)) {
            map[AppConst.UA_NAME] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }
        return map
    }
}

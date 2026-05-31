package io.legado.server

import java.util.concurrent.ConcurrentHashMap

object CacheManager {
    private val memoryCache = ConcurrentHashMap<String, Any>()

    fun putMemory(key: String, value: Any) {
        memoryCache[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getMemory(key: String): T? {
        return memoryCache[key] as? T
    }

    fun deleteMemory(key: String) {
        memoryCache.remove(key)
    }

    fun remove(key: String) {
        memoryCache.remove(key)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getFromMemory(key: String): T? {
        return memoryCache[key] as? T
    }
}

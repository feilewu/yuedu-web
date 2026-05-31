package io.legado.server.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken

private val kotlinLazyExclusion = object : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean =
        f.name.endsWith("\$delegate")
    override fun shouldSkipClass(clazz: Class<*>): Boolean = false
}

val GSON: Gson = GsonBuilder()
    .disableHtmlEscaping()
    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .addSerializationExclusionStrategy(kotlinLazyExclusion)
    .addDeserializationExclusionStrategy(kotlinLazyExclusion)
    .create()

val GSONStrict: Gson = GsonBuilder().disableHtmlEscaping().create()

inline fun <reified T> Gson.fromJsonObject(json: String?): Result<T> {
    return runCatching {
        if (json == null) throw Exception("解析字符串为空")
        fromJson(json, object : TypeToken<T>() {}.type) as T
    }
}

inline fun <reified T> Gson.fromJsonArray(json: String?): Result<List<T>> {
    return runCatching {
        if (json == null) throw Exception("解析字符串为空")
        val type = object : TypeToken<List<T>>() {}.type
        fromJson<List<T>>(json, type)
    }
}

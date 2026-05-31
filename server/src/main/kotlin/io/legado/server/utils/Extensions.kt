package io.legado.server.utils

import java.net.URI

fun String.isJson(): Boolean {
    val s = trim()
    return s.startsWith("{") && s.endsWith("}") || s.startsWith("[") && s.endsWith("]")
}

fun String.isJsonObject(): Boolean {
    val s = trim()
    return s.startsWith("{") && s.endsWith("}")
}

fun String.isJsonArray(): Boolean {
    val s = trim()
    return s.startsWith("[") && s.endsWith("]")
}

fun String.isXml(): Boolean {
    val s = trim()
    return s.startsWith("<") && s.endsWith(">")
}

fun String.isDataUrl(): Boolean {
    return startsWith("data:")
}

fun String.splitNotBlank(vararg delimiters: String): List<String> {
    return split(*delimiters).filter { it.isNotBlank() }
}

fun String.stackTraceStr(): String = this

val Throwable.stackTraceStr: String get() = stackTraceToString()

fun <T> Iterable<T>.mapAsync(concurrentCount: Int, block: suspend (T) -> kotlin.Unit): kotlinx.coroutines.flow.Flow<kotlin.Unit> {
    return kotlinx.coroutines.flow.emptyFlow()
}

suspend fun <T, R> Iterable<T>.mapParallelSafe(block: suspend (T) -> R): List<R> {
    return map { block(it) }
}

fun <K, V> MutableMap<K, V>.getOrPutLimit(key: K, limit: Int, defaultValue: () -> V): V {
    return getOrPut(key) { defaultValue() }
}

fun String?.getOrPutLimit(limit: Int, defaultValue: () -> String): String {
    return if (this.isNullOrBlank()) defaultValue() else this
}

fun String.isTrue(): Boolean = this == "true"

fun printOnDebug(msg: String?) {}

fun CharSequence?.memorySize(): Int = (this?.length ?: 0) * 2

object EncoderUtils {
    fun escape(value: String): String {
        return java.net.URLEncoder.encode(value, "UTF-8")
    }
}

object UrlUtil {
    fun getAbsoluteURL(baseUrl: String, relativeUrl: String?): String? {
        if (relativeUrl.isNullOrBlank()) return null
        return try {
            val base = URI(baseUrl)
            URI(base.scheme, base.authority, base.path, null, null).resolve(relativeUrl).toString()
        } catch (_: Exception) { relativeUrl }
    }
}

object StringUtils {
    val String.wordCountFormat: String get() = this
}

object MD5Utils {
    fun md5(str: String): String = java.security.MessageDigest.getInstance("MD5").digest(str.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

object HtmlFormatter
object ChineseUtils
object ArchiveUtils

object Utf8BomUtils {
    fun removeUTF8BOM(bytes: ByteArray): ByteArray {
        return if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte())
            bytes.copyOfRange(3, bytes.size) else bytes
    }
}

object EncodingDetect {
    fun detectEncoding(bytes: ByteArray): String? = null
    fun getHtmlEncode(bytes: ByteArray): String = "UTF-8"
}

object JsURL
object FileUtils

object LogUtils {
    fun d(tag: String, msg: String) = org.slf4j.LoggerFactory.getLogger(tag).debug(msg)
    fun e(tag: String, msg: String, tr: Throwable?) = org.slf4j.LoggerFactory.getLogger(tag).error(msg, tr)
}

fun String.toWebViewRequestHeaders(): Map<String, String> = mapOf()
fun String.getAbsoluteURL(baseUrl: String): String? = UrlUtil.getAbsoluteURL(baseUrl, this)

fun Any.applyCompatibilitySettings() {}
fun Any.runOnUI(block: () -> Unit) { block() }
fun Any.longToastOnUi(msg: String) {}
fun Any.toastOnUi(msg: String) {}
fun Any.sendToClip(msg: String) {}
fun Any.isMainThread(): Boolean = false

object DebugLog
object ACache

fun createFileReplace(path: String): java.io.File = java.io.File(path)
object FileDocExtensions

suspend fun <T, R> kotlinx.coroutines.flow.Flow<T>.mapParallelSafe(block: suspend (T) -> R): List<R> {
    val result = mutableListOf<R>()
    this.collect { item ->
        result.add(block(item))
    }
    return result
}

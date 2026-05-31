package io.legado.server

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.HexUtil
import com.script.rhino.rhinoContext
import com.script.rhino.rhinoContextOrNull
import io.legado.server.constant.AppConst
import io.legado.server.constant.AppLog
import io.legado.server.constant.AppPattern
import io.legado.server.model.entity.BaseSource
import io.legado.server.exception.NoStackTraceException
import io.legado.server.http.CookieStore
import io.legado.server.http.SSLHelper
import io.legado.server.http.StrResponse
import io.legado.server.Debug
import io.legado.server.analyzeRule.AnalyzeUrl
import io.legado.server.analyzeRule.QueryTTF
import io.legado.server.utils.GSON
import io.legado.server.utils.MD5Utils
import io.legado.server.utils.UrlUtil
import io.legado.server.utils.isAbsUrl
import io.legado.server.utils.stackTraceStr
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface JsExtensions {

    fun getSource(): BaseSource?

    private val context: CoroutineContext
        get() = rhinoContext.coroutineContext ?: EmptyCoroutineContext

    fun ajax(url: Any): String? {
        val urlStr = if (url is List<*>) {
            url.firstOrNull().toString()
        } else {
            url.toString()
        }
        val analyzeUrl = AnalyzeUrl(urlStr, source = getSource(), coroutineContext = context)
        return kotlin.runCatching {
            analyzeUrl.getStrResponse().body
        }.onFailure {
            AppLog.put("ajax(${urlStr}) error\n${it.localizedMessage}", it)
        }.getOrElse {
            it.stackTraceStr
        }
    }

    fun ajaxAll(urlList: Array<String>): Array<StrResponse> {
        return runBlocking(context) {
            urlList.asFlow().flowOn(IO).toList().map { url ->
                val analyzeUrl = AnalyzeUrl(url, source = getSource(), coroutineContext = coroutineContext)
                analyzeUrl.getStrResponse()
            }.toTypedArray()
        }
    }

    fun cache(key: String?, value: String?, cacheTime: Long = -1): String? {
        return null
    }

    fun sourceVariable(key: String?): String? {
        return null
    }

    fun setSourceVariable(key: String, value: String) {}

    fun charCodeAt(str: String?, index: Int): Int {
        if (str.isNullOrEmpty()) return 0
        return str.codePointAt(0)
    }

    fun toBase64(str: String?): String? {
        return str?.let { Base64.encode(it.toByteArray()) }
    }

    fun toBase64(str: String?, charset: String?): String? {
        return str?.let { Base64.encode(it.toByteArray(Charset.forName(charset ?: "UTF-8"))) }
    }

    fun fromBase64(base64: String?): String? {
        return base64?.let { String(Base64.decode(it)) }
    }

    fun fromBase64(base64: String?, charset: String?): String? {
        return base64?.let { String(Base64.decode(it), Charset.forName(charset ?: "UTF-8")) }
    }

    fun md5Encode(str: String?): String? {
        return str?.let { MD5Utils.md5(it) }
    }

    fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun <T> formatDate(date: T, format: String? = "yyyy-MM-dd HH:mm:ss"): String? {
        return when (date) {
            is String -> {
                kotlin.runCatching {
                    val parser = SimpleDateFormat(format, Locale.getDefault())
                    parser.parse(date)
                }.getOrNull()?.let {
                    SimpleDateFormat(format, Locale.getDefault()).format(it)
                }
            }
            is Long -> SimpleDateFormat(format, Locale.getDefault()).format(Date(date))
            is Date -> SimpleDateFormat(format, Locale.getDefault()).format(date)
            else -> null
        }
    }

    fun showBrowser(url: String?) {
        throw UnsupportedOperationException("showBrowser not supported on server")
    }

    fun copyText(text: String?) {}

    fun ocr(url: String?): String {
        throw UnsupportedOperationException("ocr not supported on server")
    }

    fun getUA(): String {
        return getHeaderMap().filter { it.key == AppConst.UA_NAME }.map { it.value }.firstOrNull()
            ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    fun sourceRegex(url: String?, sourceRegex: String?): String? {
        if (url == null || sourceRegex == null) return null
        val matcher = sourceRegex.toRegex().find(url)
        return matcher?.groupValues?.getOrNull(1)
    }

    fun addHttp(url: String?): String? {
        url ?: return null
        if (url.startsWith("http")) return url
        return "http://$url"
    }

    fun log(msg: Any?): Any? {
        AppLog.put(msg?.toString())
        return msg
    }

    fun put(key: String, value: String): String {
        return value
    }

    fun get(key: String): String? {
        return null
    }

    fun getHeaderMap(): Map<String, String> {
        return getSource()?.getHeaderMap() ?: mapOf()
    }
}

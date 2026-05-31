package io.legado.server.http

import io.legado.server.exception.NoStackTraceException

class BackstageWebView(
    private val url: String? = null,
    private val html: String? = null,
    private val encode: String? = null,
    private val tag: String? = null,
    private val headerMap: Map<String, String>? = null,
    private val sourceRegex: String? = null,
    private val overrideUrlRegex: String? = null,
    private val javaScript: String? = null,
    private val delayTime: Long = 0
) {
    private var result: String? = null

    suspend fun getStrResponse(): StrResponse {
        val body = html ?: throw NoStackTraceException("BackstageWebView not available on server")
        return StrResponse(url ?: "about:blank", body)
    }

    suspend fun getResult(): String {
        val body = html ?: throw NoStackTraceException("BackstageWebView not available on server")
        result = body
        return body
    }

    fun getResultSync(): String? = result
}

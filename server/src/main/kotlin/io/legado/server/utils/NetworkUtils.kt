package io.legado.server.utils

import java.net.URI

object NetworkUtils {
    fun getSubDomain(url: String): String {
        return try {
            val host = URI(url).host ?: return url
            host
        } catch (_: Exception) { url }
    }

    fun isAbsUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun getAbsoluteURL(baseUrl: String?, relativeUrl: String?): String? {
        if (relativeUrl.isNullOrBlank() || baseUrl.isNullOrBlank()) return relativeUrl
        return try {
            val base = URI(baseUrl)
            URI(base.scheme, base.authority, base.path, null, null).resolve(relativeUrl).toString()
        } catch (_: Exception) { relativeUrl }
    }

    fun getBaseUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            val uri = URI(url)
            "${uri.scheme}://${uri.host}"
        } catch (_: Exception) { null }
    }

    fun encodedQuery(params: String): Boolean {
        return params.contains("%") || params.contains("+")
    }

    fun encodedForm(value: String): Boolean {
        return value.contains("%") || value.contains("+")
    }
}

fun String.isAbsUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

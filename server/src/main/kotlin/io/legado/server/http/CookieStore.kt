@file:Suppress("unused")
package io.legado.server.http

import io.legado.server.constant.AppLog
import io.legado.server.constant.AppPattern.equalsRegex
import io.legado.server.constant.AppPattern.semicolonRegex
import io.legado.server.CacheManager
import io.legado.server.http.CookieManager.getCookieNoSession
import io.legado.server.http.CookieManager.mergeCookiesToMap
import io.legado.server.http.api.CookieManagerInterface
import io.legado.server.utils.NetworkUtils

object CookieStore : CookieManagerInterface {

    override fun setCookie(url: String, cookie: String?) {
        try {
            val domain = NetworkUtils.getSubDomain(url)
            CacheManager.putMemory("${domain}_cookie", cookie ?: "")
        } catch (e: Exception) {
            AppLog.put("保存Cookie失败\n$e", e)
        }
    }

    override fun replaceCookie(url: String, cookie: String) {
        if (url.isNullOrEmpty() || cookie.isNullOrEmpty()) {
            return
        }
        val oldCookie = getCookieNoSession(url)
        if (oldCookie.isNullOrEmpty()) {
            setCookie(url, cookie)
        } else {
            val cookieMap = cookieToMap(oldCookie)
            cookieMap.putAll(cookieToMap(cookie))
            val newCookie = mapToCookie(cookieMap)
            setCookie(url, newCookie)
        }
    }

    override fun getCookie(url: String): String {
        val domain = NetworkUtils.getSubDomain(url)
        val cookie = getCookieNoSession(url)
        val sessionCookie = CookieManager.getSessionCookie(domain)
        val cookieMap = mergeCookiesToMap(cookie, sessionCookie)
        var ck = mapToCookie(cookieMap) ?: ""
        while (ck.length > 4096) {
            val removeKey = cookieMap.keys.random()
            CookieManager.removeCookie(url, removeKey)
            cookieMap.remove(removeKey)
            ck = mapToCookie(cookieMap) ?: ""
        }
        return ck
    }

    fun getKey(url: String, key: String): String {
        val cookie = getCookie(url)
        val sessionCookie = CookieManager.getSessionCookie(url)
        val cookieMap = mergeCookiesToMap(cookie, sessionCookie)
        return cookieMap[key] ?: ""
    }

    override fun removeCookie(url: String) {
        val domain = NetworkUtils.getSubDomain(url)
        CacheManager.remove("${domain}_cookie")
        CacheManager.remove("${domain}_session_cookie")
    }

    override fun cookieToMap(cookie: String): MutableMap<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        if (cookie.isBlank()) {
            return cookieMap
        }
        val pairArray = cookie.split(semicolonRegex).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairArray) {
            val pairs = pair.split(equalsRegex, 2).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (pairs.size <= 1) {
                continue
            }
            cookieMap[pairs[0].trim()] = pairs[1].trim()
        }
        return cookieMap
    }

    override fun mapToCookie(map: Map<String, String>?): String? {
        val m = map ?: return null
        if (m.isEmpty()) return null
        return m.map { "${it.key}=${it.value}" }.joinToString("; ")
    }
}

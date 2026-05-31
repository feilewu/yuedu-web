package io.legado.server.http.api

interface CookieManagerInterface {
    fun setCookie(url: String, cookie: String?)
    fun replaceCookie(url: String, cookie: String)
    fun getCookie(url: String): String
    fun removeCookie(url: String)
    fun cookieToMap(cookie: String): MutableMap<String, String>
    fun mapToCookie(cookieMap: Map<String, String>?): String?
}

package io.legado.server.config

object AppConfig {
    var threadCount = 4
    var recordHttpLog = false
    var proxyEnabled = false
    var proxyHost = ""
    var proxyPort = 0
    var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    var isCronet = false

    val threadPoolSize: Int get() = threadCount

    fun isProxyEnabled(): Boolean = proxyEnabled
    fun getProxy(): String? = if (proxyEnabled && proxyHost.isNotBlank()) "$proxyHost:$proxyPort" else null
}

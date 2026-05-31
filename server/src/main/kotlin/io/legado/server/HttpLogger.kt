package io.legado.server

object HttpLogger {
    private var nextId = 0L
    fun nextId(): Long = nextId++
    fun add(record: HttpRecord) {}
}

data class HttpRecord(
    val id: Long,
    val time: Long,
    val method: String,
    val url: String,
    val statusCode: Int,
    val duration: Long,
    val requestHeaders: String,
    val requestBody: String?,
    val responseHeaders: String?,
    val responseBody: String?,
    val error: String? = null
) {
    val summary: String get() = "$method $url $statusCode ${duration}ms"
    companion object {
        const val LOG_PREFIX = "[HTTP]"
    }
}

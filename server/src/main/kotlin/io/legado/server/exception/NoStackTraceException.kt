package io.legado.server.exception

open class NoStackTraceException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun fillInStackTrace(): Throwable = this
}

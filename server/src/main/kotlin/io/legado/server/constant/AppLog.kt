package io.legado.server.constant

import org.slf4j.LoggerFactory

object AppLog {
    private val logger = LoggerFactory.getLogger("AppLog")
    private val mLogs = arrayListOf<Triple<Long, String, Throwable?>>()
    val logs get() = mLogs.toList()

    @Synchronized
    fun put(message: String?, throwable: Throwable? = null) {
        message ?: return
        mLogs.add(0, Triple(System.currentTimeMillis(), message, throwable))
        if (mLogs.size > 100) {
            mLogs.removeLastOrNull()
        }
        logger.warn(message)
        throwable?.let { logger.warn(message, it) }
    }

    @Synchronized
    fun putDebug(message: String?) {
        message ?: return
        logger.debug(message)
    }
}

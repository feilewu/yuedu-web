package io.legado.server

import org.slf4j.LoggerFactory

object Debug {
    private val logger = LoggerFactory.getLogger("Debug")

    fun log(msg: String?) {
        msg ?: return
        logger.info(msg)
    }

    fun log(tag: String?, msg: String?, state: Int = 0) {
        logger.info("[$tag] $msg")
    }

    fun log(tag: String?, msg: String?, log: Boolean) {
        if (log) {
            logger.info("[$tag] $msg")
        }
    }
}

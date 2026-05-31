package io.legado.server

import io.legado.server.model.entity.BaseSource
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class ConcurrentRateLimiter(source: BaseSource? = null, maxConcurrent: Int = 3) {
    private val semaphore = Semaphore(maxConcurrent)

    suspend fun <T> run(block: suspend () -> T): T {
        return semaphore.withPermit { block() }
    }

    suspend fun <T> withLimit(block: suspend () -> T): T {
        return semaphore.withPermit { block() }
    }

    fun <T> withLimitBlocking(block: () -> T): T {
        return block()
    }
}

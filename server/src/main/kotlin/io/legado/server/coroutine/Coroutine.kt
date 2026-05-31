package io.legado.server.coroutine

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Coroutine<T>(private val deferred: Deferred<T>) {
    companion object {
        fun <T> async(
            scope: CoroutineScope,
            context: CoroutineContext = Dispatchers.IO,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            executeContext: CoroutineContext = Dispatchers.Default,
            block: suspend CoroutineScope.() -> T
        ): Coroutine<T> {
            return Coroutine(scope.async(context, start) { block() })
        }
    }

    suspend fun await(): T = deferred.await()
}

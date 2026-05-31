package io.legado.server.web.socket

import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import io.legado.server.db.dao.BookSourceDao
import io.legado.server.utils.GSON
import io.legado.server.utils.isJson
import io.legado.server.webBook.WebBook
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.IOException

class BookSearchWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
    NanoWSD.WebSocket(handshakeRequest),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val normalClosure = NanoWSD.WebSocketFrame.CloseCode.NormalClosure
    private var job: Job? = null
    private val concurrencyLimit = 20

    override fun onOpen() {
        job = launch(IO) {
            kotlin.runCatching {
                while (isOpen) {
                    ping("ping".toByteArray())
                    delay(30000)
                }
            }
        }
    }

    override fun onClose(
        code: NanoWSD.WebSocketFrame.CloseCode,
        reason: String,
        initiatedByRemote: Boolean
    ) {
        job?.cancel()
    }

    override fun onMessage(message: NanoWSD.WebSocketFrame) {
        launch(IO) {
            kotlin.runCatching {
                if (!message.textPayload.isJson()) {
                    send("数据必须为Json格式")
                    close(normalClosure, "Search finish", false)
                    return@launch
                }
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val searchMap: Map<String, String>? = GSON.fromJson(message.textPayload, mapType)
                if (searchMap != null) {
                    val key = searchMap["key"]
                    if (key.isNullOrBlank()) {
                        send("关键词不能为空")
                        close(normalClosure, "Search finish", false)
                        return@launch
                    }
                    val sources = BookSourceDao.findAllEnabled()
                    val semaphore = Semaphore(concurrencyLimit)
                    coroutineScope {
                        for (source in sources) {
                            launch(IO) {
                                semaphore.withPermit {
                                    try {
                                        val results = WebBook.searchBookAwait(source, key)
                                        if (results.isNotEmpty()) {
                                            send(GSON.toJson(results))
                                        }
                                        } catch (_: Exception) { }
                                            yield()
                                }
                            }
                        }
                    }
                    close(normalClosure, "Search finish", false)
                } else {
                    send("数据必须为Json格式")
                    close(normalClosure, "Search finish", false)
                }
            }
        }
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) { }

    override fun onException(exception: IOException) {
        job?.cancel()
    }
}

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
import java.io.IOException

class BookSourceDebugWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
    NanoWSD.WebSocket(handshakeRequest),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private var job: Job? = null

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
                    close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                    return@launch
                }
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val debugMap: Map<String, String>? = GSON.fromJson(message.textPayload, mapType)
                if (debugMap != null) {
                    val tag = debugMap["tag"]
                    val key = debugMap["key"]
                    if (tag.isNullOrBlank() || key.isNullOrBlank()) {
                        send("参数不能为空")
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                        return@launch
                    }
                    val source = BookSourceDao.findByUrl(tag)
                    if (source != null) {
                        io.legado.server.Debug.log(tag, "开始调试书源: ${source.bookSourceName}")
                        send("开始调试书源: ${source.bookSourceName}")
                        try {
                            val results = WebBook.searchBookAwait(source, key)
                            io.legado.server.Debug.log(tag, "搜索完成, 找到 ${results.size} 条结果")
                            send("搜索完成, 找到 ${results.size} 条结果")
                            for (book in results) {
                                val msg = "${book.name} - ${book.author} (${book.originName})"
                                io.legado.server.Debug.log(tag, msg)
                                send(msg)
                            }
                        } catch (e: Exception) {
                            io.legado.server.Debug.log(tag, "调试出错: ${e.message}")
                            send("调试出错: ${e.message}")
                        }
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                    } else {
                        send("未找到书源: $tag")
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                    }
                } else {
                    send("数据必须为Json格式")
                    close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                }
            }
        }
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) { }

    override fun onException(exception: IOException) {
        job?.cancel()
    }
}

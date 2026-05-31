package io.legado.server.web.socket

import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import io.legado.server.db.dao.RssSourceDao
import io.legado.server.utils.GSON
import io.legado.server.utils.isJson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException

class RssSourceDebugWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
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
                    if (tag.isNullOrBlank()) {
                        send("参数不能为空")
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                        return@launch
                    }
                    val source = RssSourceDao.findByUrl(tag)
                    if (source != null) {
                        io.legado.server.Debug.log(tag, "RSS调试: ${source.sourceName}")
                        send("RSS调试功能暂未完整实现: ${source.sourceName}")
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                    } else {
                        send("未找到订阅源: $tag")
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

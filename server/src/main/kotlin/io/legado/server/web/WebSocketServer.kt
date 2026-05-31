package io.legado.server.web

import fi.iki.elonen.NanoWSD
import io.legado.server.web.socket.BookSearchWebSocket
import io.legado.server.web.socket.BookSourceDebugWebSocket
import io.legado.server.web.socket.RssSourceDebugWebSocket
import java.io.File

class WebSocketServer(port: Int, private val dataDir: File) : NanoWSD(port) {

    override fun openWebSocket(handshake: IHTTPSession): WebSocket? {
        return when (handshake.uri) {
            "/bookSourceDebug" -> BookSourceDebugWebSocket(handshake)
            "/rssSourceDebug" -> RssSourceDebugWebSocket(handshake)
            "/searchBook" -> BookSearchWebSocket(handshake)
            else -> null
        }
    }
}

package io.legado.server

import io.legado.server.config.ServerConfig
import io.legado.server.db.Database
import io.legado.server.web.HttpServer
import io.legado.server.web.WebSocketServer
import org.slf4j.LoggerFactory

fun main() {
    val config = ServerConfig.fromEnv()
    val logger = LoggerFactory.getLogger("App")

    println("Starting yuedu-web...")
    println("  HTTP port: ${config.httpPort}")
    println("  Data dir:  ${config.dataDir}")
    println("  Web root:  ${config.webRoot}")

    Database.init(config.dataDir.absolutePath)

    val httpServer = HttpServer(config.httpPort, config.webRoot, config.dataDir)
    httpServer.start()
    println("HTTP server started on port ${config.httpPort}")

    val wsServer = WebSocketServer(config.httpPort + 1, config.dataDir)
    wsServer.start()
    println("WebSocket server started on port ${config.httpPort + 1}")

    Runtime.getRuntime().addShutdownHook(Thread {
        httpServer.stop()
        wsServer.stop()
        Database.close()
        logger.info("Server stopped")
    })

    println("=".repeat(50))
    println("  Open http://localhost:${config.httpPort} in browser")
    println("=".repeat(50))

    Thread.currentThread().join()
}

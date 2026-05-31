package io.legado.server.config

import java.io.File

data class ServerConfig(
    val httpPort: Int = 1122,
    val dataDir: File = File(System.getProperty("user.home"), ".yuedu-web"),
    val webRoot: File = File(System.getProperty("yuedu.web.root", System.getProperty("user.dir")), "web")
) {
    init {
        dataDir.mkdirs()
        webRoot.mkdirs()
    }

    companion object {
        fun fromEnv(): ServerConfig {
            val port = System.getenv("YUEDU_PORT")?.toIntOrNull() ?: 1122
            val dataDir = System.getenv("YUEDU_DATA_DIR")?.let { File(it) }
                ?: File(System.getProperty("user.home"), ".yuedu-web")
            val webRoot = System.getenv("YUEDU_WEB_ROOT")?.let { File(it) }
                ?: File(System.getProperty("yuedu.web.root", System.getProperty("user.dir")), "web")
            return ServerConfig(httpPort = port, dataDir = dataDir, webRoot = webRoot)
        }
    }
}

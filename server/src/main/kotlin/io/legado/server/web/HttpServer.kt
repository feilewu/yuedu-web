package io.legado.server.web

import fi.iki.elonen.NanoHTTPD
import io.legado.server.utils.GSON
import io.legado.server.utils.LogUtils
import io.legado.server.utils.stackTraceStr
import io.legado.server.web.controller.BookController
import io.legado.server.web.controller.BookSourceController
import io.legado.server.web.controller.ReplaceRuleController
import io.legado.server.web.controller.RssSourceController
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class HttpServer(port: Int, private val webRoot: File, private val dataDir: File) : NanoHTTPD(port) {
    private val bookController = BookController(dataDir)

    override fun serve(session: IHTTPSession): Response {
        var returnData: ReturnData? = null
        var uri = session.uri

        try {
            when (session.method) {
                Method.OPTIONS -> {
                    val response = newFixedLengthResponse(Response.Status.OK, "text/plain", "")
                    response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    response.addHeader("Access-Control-Allow-Headers", "content-type")
                    response.addHeader("Access-Control-Allow-Origin", session.headers["origin"] ?: "*")
                    return response
                }

                Method.POST -> {
                    // Force UTF-8 encoding for body parsing (NanoHTTPD defaults to ISO-8859-1)
                    val ct = session.headers["content-type"] ?: ""
                    if (!ct.contains("charset", ignoreCase = true)) {
                        session.headers["content-type"] = "$ct; charset=utf-8"
                    }
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val postData = files["postData"]

                    returnData = when (uri) {
                        "/saveBookSource" -> BookSourceController.saveSource(postData)
                        "/saveBookSources" -> BookSourceController.saveSources(postData)
                        "/deleteBookSources" -> BookSourceController.deleteSources(postData)
                        "/saveBook" -> bookController.saveBook(postData)
                        "/deleteBook" -> bookController.deleteBook(postData)
                        "/saveBookProgress" -> bookController.saveBookProgress(postData)
                        "/saveReadConfig" -> bookController.saveReadConfig(postData)
                        "/saveRssSource" -> RssSourceController.saveSource(postData)
                        "/saveRssSources" -> RssSourceController.saveSources(postData)
                        "/deleteRssSources" -> RssSourceController.deleteSources(postData)
                        "/saveReplaceRule" -> ReplaceRuleController.saveRule(postData)
                        "/deleteReplaceRule" -> ReplaceRuleController.delete(postData)
                        "/testReplaceRule" -> ReplaceRuleController.testRule(postData)
                        else -> null
                    }
                }

                Method.GET -> {
                    val parameters = session.parameters

                    returnData = when (uri) {
                        "/proxy" -> {
                            val url = parameters["url"]?.firstOrNull()
                            if (url.isNullOrEmpty()) ReturnData.error("url 参数为空")
                            else try {
                                val response = okhttp3.OkHttpClient().newCall(
                                    okhttp3.Request.Builder().url(url).build()
                                ).execute()
                                val body = response.body?.string() ?: ""
                                ReturnData.success(body)
                            } catch (e: Exception) {
                                ReturnData.error(e.localizedMessage ?: "proxy error")
                            }
                        }
                        "/getBookSource" -> BookSourceController.getSource(parameters)
                        "/getBookSources" -> BookSourceController.getSources()
                        "/getBookshelf" -> bookController.getBookshelf()
                        "/getChapterList" -> bookController.getChapterList(parameters)
                        "/refreshToc" -> bookController.refreshToc(parameters)
                        "/getBookContent" -> bookController.getBookContent(parameters)
                        "/cover" -> bookController.getCover(parameters)
                        "/image" -> bookController.getImg(parameters)
                        "/getReadConfig" -> bookController.getReadConfig()
                        "/getRssSource" -> RssSourceController.getSource(parameters)
                        "/getRssSources" -> RssSourceController.getSources()
                        "/getReplaceRules" -> ReplaceRuleController.getAllRules()
                        else -> null
                    }
                }

                else -> Unit
            }

            if (returnData == null) {
                return serveStatic(uri)
            }

            val response = newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                GSON.toJson(returnData)
            )
            response.addHeader("Access-Control-Allow-Methods", "GET, POST")
            response.addHeader("Access-Control-Allow-Origin", session.headers["origin"] ?: "*")
            return response
        } catch (e: Exception) {
            val errorResponse = ReturnData.error(e.localizedMessage ?: "Unknown error")
            val response = newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                GSON.toJson(errorResponse)
            )
            response.addHeader("Access-Control-Allow-Origin", session.headers["origin"] ?: "*")
            return response
        }
    }

    private fun serveStatic(uri: String): Response {
        val path = if (uri == "/" || uri.isEmpty()) {
            "/index.html"
        } else if (uri.endsWith("/")) {
            "$uri/index.html"
        } else {
            uri
        }
        val file = File(webRoot, "vue$path")
        if (file.exists() && file.isFile) {
            val mime = getMimeType(path)
            val response = newChunkedResponse(
                Response.Status.OK,
                mime,
                FileInputStream(file)
            )
            response.addHeader("Access-Control-Allow-Origin", "*")
            return response
        }
        return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            "text/plain",
            "Not Found"
        )
    }

    private fun getMimeType(path: String): String {
        val suffix = path.substringAfterLast('.', "")
        return when (suffix.lowercase()) {
            "html", "htm" -> "text/html"
            "js" -> "text/javascript"
            "css" -> "text/css"
            "ico" -> "image/x-icon"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            "json" -> "application/json"
            "woff2" -> "font/woff2"
            "woff" -> "font/woff"
            "ttf" -> "font/ttf"
            "map" -> "application/json"
            else -> "text/html"
        }
    }

}

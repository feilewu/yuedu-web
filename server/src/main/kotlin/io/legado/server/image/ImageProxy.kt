package io.legado.server.image

import io.legado.server.http.okHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

class ImageProxy(cacheDir: File) {
    private val cache = ImageCache(cacheDir)

    fun getImage(url: String, maxWidth: Int = 0): ByteArray {
        val cached = cache.get(url)
        if (cached != null) {
            return if (maxWidth <= 0) cached else resizeIfNeeded(cached, maxWidth)
        }

        val bytes = downloadImage(url)
        cache.put(url, bytes)

        return if (maxWidth <= 0) bytes else resizeIfNeeded(bytes, maxWidth)
    }

    fun getImageFromPath(path: String, maxWidth: Int = 0): ByteArray {
        val cached = cache.get(path)
        if (cached != null) {
            return if (maxWidth <= 0) cached else resizeIfNeeded(cached, maxWidth)
        }

        val file = File(path)
        if (file.exists()) {
            val bytes = file.readBytes()
            cache.put(path, bytes)
            return if (maxWidth <= 0) bytes else resizeIfNeeded(bytes, maxWidth)
        }

        val urlBytes = downloadImage(path)
        cache.put(path, urlBytes)
        return if (maxWidth <= 0) urlBytes else resizeIfNeeded(urlBytes, maxWidth)
    }

    private fun resizeIfNeeded(bytes: ByteArray, maxWidth: Int): ByteArray {
        try {
            val input = java.io.ByteArrayInputStream(bytes)
            val img = ImageIO.read(input)
            input.close()
            if (img == null) return bytes
            val w = img.width
            val h = img.height
            if (w <= maxWidth) return bytes
            val newH = (h * maxWidth.toDouble() / w).toInt()
            val resized = java.awt.image.BufferedImage(maxWidth, newH, java.awt.image.BufferedImage.TYPE_INT_ARGB)
            val g = resized.createGraphics()
            g.drawImage(img, 0, 0, maxWidth, newH, null)
            g.dispose()
            val out = ByteArrayOutputStream()
            ImageIO.write(resized, "PNG", out)
            return out.toByteArray()
        } catch (_: Exception) {
            return bytes
        }
    }

    private fun downloadImage(url: String): ByteArray {
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()
        return response.body.bytes()
    }

    fun cacheClear() = cache.clear()
    fun cacheSize() = cache.size()
}

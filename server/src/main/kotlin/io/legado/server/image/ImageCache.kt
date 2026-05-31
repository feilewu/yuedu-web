package io.legado.server.image

import io.legado.server.utils.MD5Utils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImageCache(cacheDir: File) {
    private val dir = File(cacheDir, "image_cache").also { it.mkdirs() }

    fun get(url: String): ByteArray? {
        val file = getFile(url)
        if (file.exists()) {
            return file.readBytes()
        }
        return null
    }

    fun put(url: String, data: ByteArray) {
        val file = getFile(url)
        file.parentFile?.mkdirs()
        file.writeBytes(data)
    }

    fun getFile(url: String): File {
        val hash = MD5Utils.md5(url)
        return File(dir, hash)
    }

    fun size(): Long {
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    fun clear() {
        dir.deleteRecursively()
        dir.mkdirs()
    }
}

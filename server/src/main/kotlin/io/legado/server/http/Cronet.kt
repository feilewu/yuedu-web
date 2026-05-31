package io.legado.server.http

import okhttp3.Interceptor

object Cronet {
    val loader: LoaderInterface? = null

    fun preDownload() {}

    val interceptor: Interceptor? = null

    interface LoaderInterface {
        fun install(): Boolean
        fun preDownload()
    }
}

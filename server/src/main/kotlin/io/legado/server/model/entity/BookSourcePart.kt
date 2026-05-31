package io.legado.server.model.entity

data class BookSourcePart(
    val bookSource: BookSource,
    val searchUrl: String = "",
    val checkKeyWord: String? = null
)

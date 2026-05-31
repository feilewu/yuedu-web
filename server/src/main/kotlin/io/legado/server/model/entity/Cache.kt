package io.legado.server.model.entity
data class Cache(
    val key: String = "",
    var value: String? = null,
    var deadline: Long = 0L
)

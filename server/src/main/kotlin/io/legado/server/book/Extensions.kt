package io.legado.server.book

import io.legado.server.model.entity.Book

fun Book.addType(type: Int) {}
fun Book.removeAllBookType() {}
fun Book.isWebFile(): Boolean = true
fun Book.simulatedTotalChapterNum(): Int = totalChapterNum

object BookHelp {
    fun getBookTypeString(type: Int): String = ""
}

object ContentProcessor

package io.legado.server.webBook

import io.legado.server.constant.AppLog
import io.legado.server.model.entity.BookSourcePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class SearchModel(private val scope: CoroutineScope, private val callBack: CallBack) {

    private var workingState = MutableStateFlow(true)

    interface CallBack {
        fun onSearchResult(searchBooks: List<BookSourcePart>)
        fun onSearchError(msg: String)
        fun searchContent(searchBooks: List<BookSourcePart>)
    }
}

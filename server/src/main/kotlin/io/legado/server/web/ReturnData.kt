package io.legado.server.web

data class ReturnData(
    val isSuccess: Boolean = true,
    val errorMsg: String = "",
    val data: Any? = null
) {
    companion object {
        fun success(data: Any? = null): ReturnData = ReturnData(isSuccess = true, data = data)

        fun error(msg: String): ReturnData = ReturnData(isSuccess = false, errorMsg = msg)
    }
}

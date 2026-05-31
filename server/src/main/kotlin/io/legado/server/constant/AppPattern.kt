package io.legado.server.constant

import java.util.regex.Pattern

object AppPattern {
    val JS_PATTERN: Pattern = Pattern.compile("<js>([\\w\\W]*?)</js>|@js:([\\w\\W]*)", Pattern.CASE_INSENSITIVE)
    val EXP_PATTERN: Pattern = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}")
    val dataUriRegex = Regex("^data:.*?;base64,(.*)")
    val semicolonRegex = ";".toRegex()
    val equalsRegex = "=".toRegex()
    val spaceRegex = "\\s+".toRegex()
    val rnRegex = Regex("[\\r\\n]")
    val xmlContentTypeRegex = "(application|text)/\\w*\\+?xml.*".toRegex()
}

package io.legado.server.model.entity
data class ReplaceRule(
    var id: Long = System.currentTimeMillis(),
    var name: String = "",
    var group: String? = null,
    var pattern: String = "",
    var replacement: String = "",
    var scope: String? = null,
    var scopeTitle: Boolean = false,
    var scopeContent: Boolean = true,
    var excludeScope: String? = null,
    var isEnabled: Boolean = true,
    var isRegex: Boolean = true,
    var timeoutMillisecond: Long = 3000L,
    var order: Int = Int.MIN_VALUE
) {
    override fun equals(other: Any?): Boolean {
        if (other is ReplaceRule) {
            return other.id == id
        }
        return super.equals(other)
    }
    override fun hashCode(): Int {
        return id.hashCode()
    }
    val regex: Regex by lazy {
        pattern.toRegex()
    }
    fun getDisplayNameGroup(): String {
        return if (group.isNullOrBlank()) {
            name
        } else {
            String.format("%s (%s)", name, group)
        }
    }
    fun isValid(): Boolean {
        if (pattern.isEmpty()) {
            return false
        }
        if (isRegex) {
            try {
                java.util.regex.Pattern.compile(pattern)
            } catch (ex: java.util.regex.PatternSyntaxException) {
                return false
            }
            if (pattern.endsWith("|") && !pattern.endsWith("\\|")) {
                return false
            }
        }
        return true
    }
    fun getValidTimeoutMillisecond(): Long {
        if (timeoutMillisecond <= 0) {
            return 3000L
        }
        return timeoutMillisecond
    }
}

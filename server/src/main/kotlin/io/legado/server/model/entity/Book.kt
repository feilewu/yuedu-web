package io.legado.server.model.entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.charset.Charset
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
private const val BOOK_TYPE_TEXT = 0b1000
private const val BOOK_TYPE_IMAGE = 0b1000000
private const val BOOK_TYPE_LOCAL = 0b100000000
private const val BOOK_TYPE_LOCAL_TAG = "loc_book"
private const val BOOK_TYPE_WEBDAV_TAG = "webDav::"
private const val PAGE_ANIM_SCROLL = 3
private const val AUDIO_SKIP_INTRO_MS_DEFAULT = 0
private const val AUDIO_SKIP_OUTRO_MS_DEFAULT = 0
private const val AUDIO_SKIP_MIN_DURATION_MS_DEFAULT = 60000
data class Book(
    override var bookUrl: String = "",
    var tocUrl: String = "",
    var origin: String = BOOK_TYPE_LOCAL_TAG,
    var originName: String = "",
    override var name: String = "",
    override var author: String = "",
    override var kind: String? = null,
    var customTag: String? = null,
    var coverUrl: String? = null,
    var customCoverUrl: String? = null,
    var intro: String? = null,
    var customIntro: String? = null,
    var charset: String? = null,
    var type: Int = BOOK_TYPE_TEXT,
    var group: Long = 0,
    var latestChapterTitle: String? = null,
    var latestChapterTime: Long = System.currentTimeMillis(),
    var lastCheckTime: Long = System.currentTimeMillis(),
    var lastCheckCount: Int = 0,
    var totalChapterNum: Int = 0,
    var durChapterTitle: String? = null,
    var durChapterIndex: Int = 0,
    var durChapterPos: Int = 0,
    var durChapterTime: Long = System.currentTimeMillis(),
    override var wordCount: String? = null,
    var canUpdate: Boolean = true,
    var order: Int = 0,
    var originOrder: Int = 0,
    override var variable: String? = null,
    var readConfig: ReadConfig? = null,
    var syncTime: Long = 0L
) : BaseBook {
    override fun equals(other: Any?): Boolean {
        if (other is Book) {
            return other.bookUrl == bookUrl
        }
        return false
    }
    override fun hashCode(): Int {
        return bookUrl.hashCode()
    }
    override val variableMap: HashMap<String, String> by lazy {
        try {
            Gson().fromJson(variable, object : TypeToken<HashMap<String, String>>() {}.type)
                as? HashMap<String, String> ?: hashMapOf()
        } catch (_: Exception) {
            hashMapOf()
        }
    }
    override var infoHtml: String? = null
    override var tocHtml: String? = null
    var downloadUrls: List<String>? = null
    private var folderName: String? = null
    val lastChapterIndex get() = totalChapterNum - 1
    private fun isType(flag: Int): Boolean = type and flag != 0
    private val isLocal: Boolean
        get() = (type == 0 && (origin == BOOK_TYPE_LOCAL_TAG || origin.startsWith(BOOK_TYPE_WEBDAV_TAG))) ||
                isType(BOOK_TYPE_LOCAL)
    val isImage: Boolean get() = isType(BOOK_TYPE_IMAGE)
    val isEpub: Boolean get() = isLocal && (originName.endsWith(".epub", true))
    private fun simulatedTotalChapterNum(): Int {
        return if (config.readSimulating) {
            val currentDate = LocalDate.now()
            val startDate = config.startDate ?: LocalDate.now()
            val daysPassed = java.time.Period.between(startDate, currentDate).days + 1
            val chaptersToUnlock = max(0, (config.startChapter ?: 0) + (daysPassed * config.dailyChapters))
            min(totalChapterNum, chaptersToUnlock)
        } else {
            totalChapterNum
        }
    }
    fun getUnreadChapterNum() = max(simulatedTotalChapterNum() - durChapterIndex - 1, 0)
    fun getDisplayCover() = if (customCoverUrl.isNullOrEmpty()) coverUrl else customCoverUrl
    fun getDisplayIntro() = if (customIntro.isNullOrEmpty()) intro else customIntro
    @Suppress("unused")
    fun upCustomIntro() {
        customIntro = intro
    }
    fun fileCharset(): Charset {
        return Charset.forName(charset ?: "UTF-8")
    }
    val config: ReadConfig
        get() {
            if (readConfig == null) {
                readConfig = ReadConfig()
            }
            return readConfig!!
        }
    fun setReverseToc(reverseToc: Boolean) {
        config.reverseToc = reverseToc
    }
    fun getReverseToc(): Boolean {
        return config.reverseToc
    }
    fun setUseReplaceRule(useReplaceRule: Boolean) {
        config.useReplaceRule = useReplaceRule
    }
    fun getUseReplaceRule(): Boolean {
        val useReplaceRule = config.useReplaceRule
        if (useReplaceRule != null) {
            return useReplaceRule
        }
        if (isImage || isEpub) {
            return false
        }
        return true
    }
    fun getRealAuthor() = author.replace(Regex("""^\s*作\s*者[:：\s]+|\s+著"""), "")
    fun setReSegment(reSegment: Boolean) {
        config.reSegment = reSegment
    }
    fun getReSegment(): Boolean {
        return config.reSegment
    }
    fun setPageAnim(pageAnim: Int?) {
        config.pageAnim = pageAnim
    }
    fun getPageAnim(): Int {
        var pageAnim = config.pageAnim ?: if (isImage) PAGE_ANIM_SCROLL else 0
        if (pageAnim < 0) {
            pageAnim = 0
        }
        return pageAnim
    }
    fun setImageStyle(imageStyle: String?) {
        config.imageStyle = imageStyle
    }
    fun getImageStyle(): String? {
        return config.imageStyle
    }
    fun setTtsEngine(ttsEngine: String?) {
        config.ttsEngine = ttsEngine
    }
    fun getTtsEngine(): String? {
        return config.ttsEngine
    }
    fun setAudioSkipEnabled(enabled: Boolean?) {
        config.audioSkipEnabled = enabled
    }
    fun getAudioSkipEnabled(): Boolean {
        return config.audioSkipEnabled ?: false
    }
    fun setAudioIntroMs(value: Int?) {
        config.audioIntroMs = value?.coerceAtLeast(0)
    }
    fun getAudioIntroMs(): Int {
        return config.audioIntroMs ?: AUDIO_SKIP_INTRO_MS_DEFAULT
    }
    fun setAudioOutroMs(value: Int?) {
        config.audioOutroMs = value?.coerceAtLeast(0)
    }
    fun getAudioOutroMs(): Int {
        return config.audioOutroMs ?: AUDIO_SKIP_OUTRO_MS_DEFAULT
    }
    fun getAudioSkipMinDurationMs(): Int {
        return config.audioSkipMinDurationMs ?: AUDIO_SKIP_MIN_DURATION_MS_DEFAULT
    }
    fun setSplitLongChapter(limitLongContent: Boolean) {
        config.splitLongChapter = limitLongContent
    }
    fun getSplitLongChapter(): Boolean {
        return config.splitLongChapter
    }
    fun setReadSimulating(readSimulating: Boolean) {
        config.readSimulating = readSimulating
    }
    fun getReadSimulating(): Boolean {
        return config.readSimulating
    }
    fun setStartDate(startDate: LocalDate?) {
        config.startDate = startDate
    }
    fun getStartDate(): LocalDate? {
        if (!config.readSimulating || config.startDate == null) {
            return LocalDate.now()
        }
        return config.startDate
    }
    fun setStartChapter(startChapter: Int) {
        config.startChapter = startChapter
    }
    fun getStartChapter(): Int {
        if (config.readSimulating) return config.startChapter ?: 0
        return this.durChapterIndex
    }
    fun setDailyChapters(dailyChapters: Int) {
        config.dailyChapters = dailyChapters
    }
    fun getDailyChapters(): Int {
        return config.dailyChapters
    }
    fun getDelTag(tag: Long): Boolean {
        return config.delTag and tag == tag
    }
    fun addDelTag(tag: Long) {
        config.delTag = config.delTag and tag
    }
    fun removeDelTag(tag: Long) {
        config.delTag = config.delTag and tag.inv()
    }
    fun toSearchBook() = SearchBook(
        name = name,
        author = author,
        kind = kind,
        bookUrl = bookUrl,
        origin = origin,
        originName = originName,
        type = type,
        wordCount = wordCount,
        latestChapterTitle = latestChapterTitle,
        coverUrl = coverUrl,
        intro = intro,
        tocUrl = tocUrl,
        originOrder = originOrder,
        variable = variable
    ).apply {
        this.infoHtml = this@Book.infoHtml
        this.tocHtml = this@Book.tocHtml
    }
    @Suppress("ConstPropertyName")
    companion object {
        const val hTag = 2L
        const val rubyTag = 4L
        const val imgStyleDefault = "DEFAULT"
        const val imgStyleFull = "FULL"
        const val imgStyleText = "TEXT"
        const val imgStyleSingle = "SINGLE"
    }
    data class ReadConfig(
        var reverseToc: Boolean = false,
        var pageAnim: Int? = null,
        var reSegment: Boolean = false,
        var imageStyle: String? = null,
        var useReplaceRule: Boolean? = null,
        var delTag: Long = 0L,
        var ttsEngine: String? = null,
        var audioSkipEnabled: Boolean? = null,
        var audioIntroMs: Int? = null,
        var audioOutroMs: Int? = null,
        var audioSkipMinDurationMs: Int? = null,
        var splitLongChapter: Boolean = true,
        var readSimulating: Boolean = false,
        var startDate: LocalDate? = null,
        var startChapter: Int? = null,
        var dailyChapters: Int = 3
    )
}

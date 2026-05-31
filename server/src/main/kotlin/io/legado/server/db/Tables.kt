package io.legado.server.db
object Tables {
    fun createTables() {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        stmt.executeUpdate(booksTable)
        stmt.executeUpdate(chaptersTable)
        stmt.executeUpdate(bookSourcesTable)
        stmt.executeUpdate(rssSourcesTable)
        stmt.executeUpdate(replaceRulesTable)
        stmt.executeUpdate(cachesTable)
        stmt.executeUpdate(searchBooksTable)
        stmt.close()
    }
    private val booksTable = """
        CREATE TABLE IF NOT EXISTS books (
            bookUrl TEXT PRIMARY KEY,
            tocUrl TEXT NOT NULL DEFAULT '',
            origin TEXT NOT NULL DEFAULT '',
            originName TEXT NOT NULL DEFAULT '',
            name TEXT NOT NULL DEFAULT '',
            author TEXT NOT NULL DEFAULT '',
            kind TEXT,
            customTag TEXT,
            coverUrl TEXT,
            customCoverUrl TEXT,
            intro TEXT,
            customIntro TEXT,
            charset TEXT,
            type INTEGER NOT NULL DEFAULT 0,
            group_ INTEGER NOT NULL DEFAULT 0,
            latestChapterTitle TEXT,
            latestChapterTime INTEGER NOT NULL DEFAULT 0,
            lastCheckTime INTEGER NOT NULL DEFAULT 0,
            lastCheckCount INTEGER NOT NULL DEFAULT 0,
            totalChapterNum INTEGER NOT NULL DEFAULT 0,
            durChapterTitle TEXT,
            durChapterIndex INTEGER NOT NULL DEFAULT 0,
            durChapterPos INTEGER NOT NULL DEFAULT 0,
            durChapterTime INTEGER NOT NULL DEFAULT 0,
            wordCount TEXT,
            canUpdate INTEGER NOT NULL DEFAULT 1,
            order_ INTEGER NOT NULL DEFAULT 0,
            originOrder INTEGER NOT NULL DEFAULT 0,
            variable TEXT,
            readConfig TEXT,
            syncTime INTEGER NOT NULL DEFAULT 0,
            infoHtml TEXT,
            tocHtml TEXT,
            downloadUrls TEXT
        )
    """.trimIndent()
    private val chaptersTable = """
        CREATE TABLE IF NOT EXISTS chapters (
            url TEXT PRIMARY KEY,
            title TEXT NOT NULL DEFAULT '',
            isVolume INTEGER NOT NULL DEFAULT 0,
            baseUrl TEXT NOT NULL DEFAULT '',
            bookUrl TEXT NOT NULL DEFAULT '',
            index_ INTEGER NOT NULL DEFAULT 0,
            isVip INTEGER NOT NULL DEFAULT 0,
            isPay INTEGER NOT NULL DEFAULT 0,
            resourceUrl TEXT,
            tag TEXT,
            wordCount TEXT,
            start_ INTEGER,
            end_ INTEGER,
            startFragmentId TEXT,
            endFragmentId TEXT,
            variable TEXT,
            titleMD5 TEXT
        )
    """.trimIndent()
    private val bookSourcesTable = """
        CREATE TABLE IF NOT EXISTS book_sources (
            bookSourceUrl TEXT PRIMARY KEY,
            bookSourceName TEXT NOT NULL DEFAULT '',
            bookSourceGroup TEXT,
            bookSourceType INTEGER NOT NULL DEFAULT 0,
            bookUrlPattern TEXT,
            customOrder INTEGER NOT NULL DEFAULT 0,
            enabled INTEGER NOT NULL DEFAULT 1,
            enabledExplore INTEGER NOT NULL DEFAULT 1,
            jsLib TEXT,
            enabledCookieJar INTEGER,
            concurrentRate TEXT,
            header TEXT,
            loginUrl TEXT,
            loginUi TEXT,
            loginCheckJs TEXT,
            coverDecodeJs TEXT,
            bookSourceComment TEXT,
            variableComment TEXT,
            lastUpdateTime INTEGER NOT NULL DEFAULT 0,
            respondTime INTEGER NOT NULL DEFAULT 180000,
            weight INTEGER NOT NULL DEFAULT 0,
            exploreUrl TEXT,
            exploreScreen TEXT,
            ruleExplore TEXT,
            searchUrl TEXT,
            ruleSearch TEXT,
            ruleBookInfo TEXT,
            ruleToc TEXT,
            ruleContent TEXT,
            eventListener INTEGER NOT NULL DEFAULT 0,
            customButton INTEGER NOT NULL DEFAULT 0,
            ruleReview TEXT
        )
    """.trimIndent()
    private val rssSourcesTable = """
        CREATE TABLE IF NOT EXISTS rss_sources (
            sourceUrl TEXT PRIMARY KEY,
            sourceName TEXT NOT NULL DEFAULT '',
            sourceIcon TEXT NOT NULL DEFAULT '',
            sourceGroup TEXT,
            sourceComment TEXT,
            enabled INTEGER NOT NULL DEFAULT 1,
            variableComment TEXT,
            jsLib TEXT,
            enabledCookieJar INTEGER,
            concurrentRate TEXT,
            header TEXT,
            loginUrl TEXT,
            loginUi TEXT,
            loginCheckJs TEXT,
            coverDecodeJs TEXT,
            sortUrl TEXT,
            singleUrl INTEGER NOT NULL DEFAULT 0,
            articleStyle INTEGER NOT NULL DEFAULT 0,
            ruleArticles TEXT,
            ruleNextPage TEXT,
            ruleTitle TEXT,
            rulePubDate TEXT,
            ruleDescription TEXT,
            ruleImage TEXT,
            ruleLink TEXT,
            ruleContent TEXT,
            contentWhitelist TEXT,
            contentBlacklist TEXT,
            shouldOverrideUrlLoading TEXT,
            style TEXT,
            enableJs INTEGER NOT NULL DEFAULT 1,
            loadWithBaseUrl INTEGER NOT NULL DEFAULT 1,
            injectJs TEXT,
            lastUpdateTime INTEGER NOT NULL DEFAULT 0,
            customOrder INTEGER NOT NULL DEFAULT 0
        )
    """.trimIndent()
    private val replaceRulesTable = """
        CREATE TABLE IF NOT EXISTS replace_rules (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL DEFAULT '',
            group_ TEXT,
            pattern TEXT NOT NULL DEFAULT '',
            replacement TEXT NOT NULL DEFAULT '',
            scope TEXT,
            scopeTitle INTEGER NOT NULL DEFAULT 0,
            scopeContent INTEGER NOT NULL DEFAULT 1,
            excludeScope TEXT,
            isEnabled INTEGER NOT NULL DEFAULT 1,
            isRegex INTEGER NOT NULL DEFAULT 1,
            timeoutMillisecond INTEGER NOT NULL DEFAULT 3000,
            order_ INTEGER NOT NULL DEFAULT -2147483648
        )
    """.trimIndent()
    private val cachesTable = """
        CREATE TABLE IF NOT EXISTS caches (
            key TEXT PRIMARY KEY,
            value TEXT,
            deadline INTEGER NOT NULL DEFAULT 0
        )
    """.trimIndent()
    private val searchBooksTable = """
        CREATE TABLE IF NOT EXISTS search_books (
            bookUrl TEXT PRIMARY KEY,
            origin TEXT NOT NULL DEFAULT '',
            originName TEXT NOT NULL DEFAULT '',
            type INTEGER NOT NULL DEFAULT 8,
            name TEXT NOT NULL DEFAULT '',
            author TEXT NOT NULL DEFAULT '',
            kind TEXT,
            coverUrl TEXT,
            intro TEXT,
            wordCount TEXT,
            latestChapterTitle TEXT,
            tocUrl TEXT NOT NULL DEFAULT '',
            time INTEGER NOT NULL DEFAULT 0,
            variable TEXT,
            originOrder INTEGER NOT NULL DEFAULT 0,
            chapterWordCountText TEXT,
            chapterWordCount INTEGER NOT NULL DEFAULT -1,
            respondTime INTEGER NOT NULL DEFAULT -1,
            infoHtml TEXT,
            tocHtml TEXT
        )
    """.trimIndent()
}

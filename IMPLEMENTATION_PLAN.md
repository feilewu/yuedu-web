# yuedu-web 实施计划

> **目标：** 将 Legado Android 应用的核心爬虫引擎 + REST API 提取为独立 JVM 服务端，使现有的 Vue 3 Web 前端可以脱离手机运行。
>
> **架构：** 保持原项目的 Kotlin/JVM 技术栈，NanoHTTPd 作为 HTTP 服务器（原项目已在用），用 SQLite/JDBC 替换 Room，用 `javax.imageio` 替换 Glide/Android Bitmap。复用原项目的 `modules:rhino`（JS 引擎）和 `modules:book`（电子书解析）。
>
> **技术栈：** Kotlin 2.3.0 + JVM 17 + NanoHTTPd + SQLite (JDBC) + OkHttp 5.x + JSoup + htmlunit-core-js (Rhino)

---

## 目录结构

```
yuedu-web/
├── settings.gradle.kts              # 项目配置，包含 :server 和依赖的子模块
├── build.gradle.kts                  # 根构建文件
├── gradle/
│   └── libs.versions.toml            # 版本目录（复用 legadoT 的核心依赖）
├── gradlew / gradlew.bat
├── modules/
│   ├── rhino/                        # 从 legadoT 复制，改为纯 JVM 库
│   └── book/                         # 从 legadoT 复制，改为纯 JVM 库
├── server/                           # 主服务模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/io/legado/server/
│       │   ├── App.kt                # 入口 main()
│       │   ├── config/
│       │   │   └── ServerConfig.kt   # 服务器配置（端口、数据目录等）
│       │   ├── db/
│       │   │   ├── Database.kt       # SQLite 连接管理
│       │   │   ├── Tables.kt         # 建表语句
│       │   │   └── dao/              # 数据访问层（替代原 Room DAO）
│       │   │       ├── BookDao.kt
│       │   │       ├── BookSourceDao.kt
│       │   │       ├── ChapterDao.kt
│       │   │       ├── RssSourceDao.kt
│       │   │       ├── ReplaceRuleDao.kt
│       │   │       └── CacheDao.kt
│       │   ├── model/
│       │   │   ├── entity/           # 实体类（从 legadoT data/entities/ 复制，去掉 Room 注解）
│       │   │   │   ├── Book.kt
│       │   │   │   ├── BookChapter.kt
│       │   │   │   ├── BookSource.kt
│       │   │   │   ├── RssSource.kt
│       │   │   │   ├── ReplaceRule.kt
│       │   │   │   └── SearchBook.kt
│       │   │   └── rule/              # 规则子实体
│       │   │       ├── SearchRule.kt
│       │   │       ├── BookInfoRule.kt
│       │   │       ├── ContentRule.kt
│       │   │       └── TocRule.kt
│       │   ├── analyzeRule/          # 从 legadoT 直接复制，无 Android 依赖
│       │   │   ├── AnalyzeRule.kt
│       │   │   ├── AnalyzeByJSoup.kt
│       │   │   ├── AnalyzeByXPath.kt
│       │   │   ├── AnalyzeByJSonPath.kt
│       │   │   ├── AnalyzeByRegex.kt
│       │   │   ├── AnalyzeUrl.kt
│       │   │   └── ...
│       │   ├── webBook/              # 从 legadoT 直接复制，无 Android 依赖
│       │   │   ├── WebBook.kt
│       │   │   ├── BookContent.kt
│       │   │   ├── BookChapterList.kt
│       │   │   ├── BookInfo.kt
│       │   │   ├── BookList.kt
│       │   │   ├── SearchModel.kt
│       │   │   └── ...
│       │   ├── http/                 # HTTP 客户端 + JS 扩展
│       │   │   ├── HttpHelper.kt     # 从 legadoT 复制，去 Android 依赖
│       │   │   ├── OkHttpUtils.kt
│       │   │   ├── CookieStore.kt
│       │   │   └── JsExtensions.kt   # JS 桥接（ajax/http）
│       │   ├── rhino/                # JS 引擎桥接
│       │   │   ├── NativeBaseSource.kt
│       │   │   └── SharedJsScope.kt
│       │   ├── image/                # 图片代理（替代 Glide）
│       │   │   ├── ImageProxy.kt     # 下载、缓存、缩放
│       │   │   └── ImageCache.kt     # 文件缓存
│       │   ├── service/
│       │   │   ├── BookService.kt    # 书籍 CRUD 业务逻辑
│       │   │   ├── SourceService.kt  # 书源管理
│       │   │   ├── SearchService.kt  # 多源搜索
│       │   │   ├── DebugService.kt   # 书源调试
│       │   │   └── ContentService.kt # 内容获取+缓存
│       │   ├── web/
│       │   │   ├── HttpServer.kt     # NanoHTTPd HTTP 服务器
│       │   │   ├── WebSocketServer.kt # NanoWSD WebSocket 服务器
│       │   │   ├── controller/       # API 控制器（从 legadoT 复制改造）
│       │   │   │   ├── BookController.kt
│       │   │   │   ├── BookSourceController.kt
│       │   │   │   ├── RssSourceController.kt
│       │   │   │   └── ReplaceRuleController.kt
│       │   │   └── AssetsWeb.kt      # 静态文件服务
│       │   └── utils/                # 工具类（从 legadoT 复制，去 Android 依赖）
│       │       ├── GsonExtensions.kt
│       │       ├── StringExtensions.kt
│       │       ├── RegexExtensions.kt
│       │       ├── NetworkUtils.kt
│       │       └── ...
│       └── resources/
│           └── web/                  # Vue 前端构建产物
│               └── vue/
│                   ├── index.html
│                   └── assets/
```

---

## 任务分解

### Task 1: 创建项目脚手架

**文件：**
- 创建: `yuedu-web/settings.gradle.kts`
- 创建: `yuedu-web/build.gradle.kts`
- 创建: `yuedu-web/gradle/libs.versions.toml`
- 创建: `yuedu-web/gradle/wrapper/gradle-wrapper.properties`
- 创建: `yuedu-web/server/build.gradle.kts`

**Step 1.1: 创建根级 settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "yuedu-web"
include(":server")
include(":modules:rhino")
include(":modules:book")
```

**Step 1.2: 创建版本目录 gradle/libs.versions.toml**

```toml
[versions]
kotlin = "2.3.0"
coroutines = "1.10.2"
okhttp = "5.3.2"
jsoup = "1.16.2"
gson = "2.13.2"
jsonPath = "2.10.0"
jsoupxpath = "2.5.3"
nanoHttpd = "2.3.1"
rhino = "5.0.0-legado.1"
commonsText = "1.13.1"
hutool = "5.8.22"
sqlite = "3.49.1.0"
logback = "1.5.16"
slf4j = "2.0.17"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlin-reflect = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
jsoup = { module = "org.jsoup:jsoup", version.ref = "jsoup" }
gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
json-path = { module = "com.jayway.jsonpath:json-path", version.ref = "jsonPath" }
jsoupxpath = { module = "cn.wanghaomiao:JsoupXpath", version.ref = "jsoupxpath" }
nanohttpd = { module = "org.nanohttpd:nanohttpd", version.ref = "nanoHttpd" }
nanohttpd-websocket = { module = "org.nanohttpd:nanohttpd-websocket", version.ref = "nanoHttpd" }
mozilla-rhino = { module = "org.htmlunit:htmlunit-core-js", version.ref = "rhino" }
commons-text = { module = "org.apache.commons:commons-text", version.ref = "commonsText" }
hutool-crypto = { module = "cn.hutool:hutool-crypto", version.ref = "hutool" }
sqlite-jdbc = { module = "org.xerial:sqlite-jdbc", version.ref = "sqlite" }
logback-classic = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }
slf4j-api = { module = "org.slf4j:slf4j-api", version.ref = "slf4j" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-sam = { id = "org.jetbrains.kotlin.plugin.sam.with.receiver", version.ref = "kotlin" }

[bundles]
coroutines = ["kotlinx-coroutines-core"]
```

**Step 1.3: 创建根级 build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register("buildWebFrontend") {
    group = "build"
    description = "Builds the Vue frontend"
    doLast {
        val webDir = rootProject.projectDir.resolve("modules/web")
        if (webDir.resolve("node_modules").exists()) {
            exec {
                workingDir = webDir
                commandLine("pnpm", "build")
            }
        }
    }
}
```

**Step 1.4: 创建 server/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam)
}

group = "io.legado"
version = "1.0.0"

dependencies {
    implementation(project(":modules:rhino"))
    implementation(project(":modules:book"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.coroutines)
    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.json.path)
    implementation(libs.jsoupxpath)
    implementation(libs.nanohttpd)
    implementation(libs.nanohttpd.websocket)
    implementation(libs.commons.text)
    implementation(libs.hutool.crypto)
    implementation(libs.sqlite.jdbc)
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)
}

application {
    mainClass = "io.legado.server.AppKt"
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "io.legado.server.AppKt"
    }
}
```

**Step 1.5: 生成 Gradle Wrapper**

```bash
cd /home/taole/projects/yuedu-web
gradle wrapper --gradle-version=8.12
```

---

### Task 2: 提取 modules:rhino 为纯 JVM 库

**文件：**
- 创建: `yuedu-web/modules/rhino/build.gradle.kts`
- 复制: 从 `legadoT/modules/rhino/src/` 复制所有源代码

**Step 2.1: 创建 build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.legado"
version = "1.0.0"

dependencies {
    api(libs.mozilla.rhino)
    implementation(libs.bundles.coroutines)
    implementation(libs.okhttp)
}
```

**Step 2.2: 复制源代码**

```bash
cp -r /home/taole/projects/legadoT/modules/rhino/src /home/taole/projects/yuedu-web/modules/rhino/
```

**Step 2.3: 修改 RhinoScriptEngine.kt 移除 Android 依赖**

文件: `modules/rhino/src/main/java/com/script/rhino/RhinoScriptEngine.kt`

需要移除 `androidx.collection` 的导入，将 `ArraySet` 替换为 `LinkedHashSet`。

定位到 `import androidx.collection.ArraySet` 所在行，替换为：

```kotlin
import java.util.LinkedHashSet
```

将所有 `ArraySet<` 替换为 `LinkedHashSet<`。

---

### Task 3: 提取 modules:book 为纯 JVM 库

**文件：**
- 创建: `yuedu-web/modules/book/build.gradle.kts`
- 复制: 从 `legadoT/modules/book/src/` 复制所有源代码

**Step 3.1: 创建 build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.legado"
version = "1.0.0"

dependencies {
    implementation(libs.jsoup)
}
```

**Step 3.2: 复制并移除 Android 依赖**

```bash
cp -r /home/taole/projects/legadoT/modules/book/src /home/taole/projects/yuedu-web/modules/book/
```

检查 `modules/book/src/` 下所有文件，移除 `androidx.annotation.NonNull` / `Nullable` 导入，替换为 `org.jetbrains.annotations.NotNull` / `Nullable`，或直接移除注解。

---

### Task 4: 复制核心实体和规则子实体

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/model/entity/` 下所有实体类
- 创建: `server/src/main/kotlin/io/legado/server/model/rule/` 下所有规则子实体

**Step 4.1: 从 legadoT 复制实体源码（去掉 Room 注解）**

```bash
# 创建目录
mkdir -p /home/taole/projects/yuedu-web/server/src/main/kotlin/io/legado/server/model/entity
mkdir -p /home/taole/projects/yuedu-web/server/src/main/kotlin/io/legado/server/model/rule
```

需要复制的实体（位于 `app/src/main/java/io/legado/app/data/entities/`）：

| 源文件 | 需要修改 |
|--------|---------|
| `Book.kt` | 移除 `@Entity`, `@PrimaryKey`, `@ColumnInfo`, `@Ignore`, `@Embedded` 等 Room 注解；移除 `android.os.Parcelable` 实现；将 `type` 类型从 `Int` 改为 `Int`（不变） |
| `BookChapter.kt` | 同上，移除 Parcelable |
| `BookSource.kt` | 移除 Room 注解，保留字段 |
| `RssSource.kt` | 同上 |
| `ReplaceRule.kt` | 同上 |
| `SearchBook.kt` | 移除 Room 注解 |
| `rule/SearchRule.kt` | 无 Android 依赖，直接复制 |
| `rule/BookInfoRule.kt` | 同上 |
| `rule/ContentRule.kt` | 同上 |
| `rule/TocRule.kt` | 同上 |
| `rule/ExploreRule.kt` | 同上 |
| `rule/BookListRule.kt` | 同上 |
| `rule/ReviewRule.kt` | 同上 |

**Step 4.2: 转换 Book.kt 示例**

原文件有 `@Entity`、`@PrimaryKey`、`Parcelable` 等注解。转换后：

```kotlin
package io.legado.server.model.entity

data class Book(
    val bookUrl: String = "",
    val name: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val kind: String = "",
    val intro: String = "",
    val origin: String = "",
    val originName: String = "",
    val type: Int = 0,
    val group: Int = 0,
    val durChapterTitle: String = "",
    val durChapterIndex: Int = 0,
    val durChapterPos: Int = 0,
    val durChapterTime: Long = 0,
    val latestChapterTitle: String = "",
    val latestChapterTime: Long = 0,
    val totalChapterNum: Int = 0,
    val wordCount: String = "",
    val canUpdate: Boolean = true,
    val customCoverUrl: String = "",
    val customIntro: String = "",
    val variable: String = "{}",
    val readConfig: String = "{}",
    val order: Int = 0,
    val originOrder: Int = 0,
    val syncTime: Long = 0,
    val lastCheckTime: Long = 0,
    val lastCheckCount: Int = 0,
    val tocUrl: String = "",
    val charset: String = "",
    val customTag: String = ""
)
```

**Step 4.3: 转换 BookSource.kt 示例**

```kotlin
package io.legado.server.model.entity

data class BookSource(
    val bookSourceUrl: String = "",
    val bookSourceName: String = "",
    val bookSourceGroup: String = "",
    val bookSourceType: Int = 0,
    val bookUrlPattern: String = "",
    val customOrder: Int = 0,
    val enabled: Boolean = true,
    val enabledExplore: Boolean = false,
    val jsLib: String = "",
    val enabledCookieJar: Boolean = true,
    val concurrentRate: String = "",
    val header: String = "{}",
    val loginUrl: String = "",
    val loginUi: String = "",
    val loginCheckJs: String = "",
    val coverDecodeJs: String = "",
    val bookSourceComment: String = "",
    val variableComment: String = "",
    val lastUpdateTime: Long = 0,
    val respondTime: Long = 0,
    val weight: Int = 0,
    val exploreUrl: String = "",
    val exploreScreen: String = "",
    val ruleExplore: String = "{}",
    val searchUrl: String = "",
    val ruleSearch: String = "{}",
    val ruleBookInfo: String = "{}",
    val ruleToc: String = "{}",
    val ruleContent: String = "{}",
    val eventListener: Boolean = false,
    val customButton: Boolean = false,
    val ruleReview: String = "{}"
)
```

---

### Task 5: 实现数据库层

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/db/Database.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/Tables.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/BookDao.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/BookSourceDao.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/ChapterDao.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/RssSourceDao.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/ReplaceRuleDao.kt`
- 创建: `server/src/main/kotlin/io/legado/server/db/dao/CacheDao.kt`

**Step 5.1: Database.kt — SQLite 连接管理**

```kotlin
package io.legado.server.db

import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val logger = LoggerFactory.getLogger(Database::class.java)
    private var connection: Connection? = null

    fun init(dbPath: String) {
        Class.forName("org.sqlite.JDBC")
        val path = File(dbPath, "legado.db").absolutePath
        connection = DriverManager.getConnection("jdbc:sqlite:$path")
        connection?.let { conn ->
            conn.createStatement().execute("PRAGMA journal_mode=WAL")
            conn.createStatement().execute("PRAGMA foreign_keys=ON")
        }
        Tables.createTables()
        logger.info("Database initialized at $path")
    }

    fun getConnection(): Connection =
        connection ?: throw IllegalStateException("Database not initialized")

    fun close() {
        connection?.close()
        connection = null
    }
}
```

**Step 5.2: Tables.kt — 建表语句**

按原 Room 数据库的 schema 创建 SQLite DDL。需要定义以下表的 CREATE TABLE：

```sql
CREATE TABLE IF NOT EXISTS books (
    bookUrl TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    author TEXT NOT NULL DEFAULT '',
    coverUrl TEXT NOT NULL DEFAULT '',
    kind TEXT NOT NULL DEFAULT '',
    intro TEXT NOT NULL DEFAULT '',
    origin TEXT NOT NULL DEFAULT '',
    originName TEXT NOT NULL DEFAULT '',
    type INTEGER NOT NULL DEFAULT 0,
    group_ INTEGER NOT NULL DEFAULT 0,
    durChapterTitle TEXT NOT NULL DEFAULT '',
    durChapterIndex INTEGER NOT NULL DEFAULT 0,
    durChapterPos INTEGER NOT NULL DEFAULT 0,
    durChapterTime INTEGER NOT NULL DEFAULT 0,
    latestChapterTitle TEXT NOT NULL DEFAULT '',
    latestChapterTime INTEGER NOT NULL DEFAULT 0,
    totalChapterNum INTEGER NOT NULL DEFAULT 0,
    wordCount TEXT NOT NULL DEFAULT '',
    canUpdate INTEGER NOT NULL DEFAULT 1,
    customCoverUrl TEXT NOT NULL DEFAULT '',
    customIntro TEXT NOT NULL DEFAULT '',
    variable TEXT NOT NULL DEFAULT '{}',
    readConfig TEXT NOT NULL DEFAULT '{}',
    order_ INTEGER NOT NULL DEFAULT 0,
    originOrder INTEGER NOT NULL DEFAULT 0,
    syncTime INTEGER NOT NULL DEFAULT 0,
    lastCheckTime INTEGER NOT NULL DEFAULT 0,
    lastCheckCount INTEGER NOT NULL DEFAULT 0,
    tocUrl TEXT NOT NULL DEFAULT '',
    charset TEXT NOT NULL DEFAULT '',
    customTag TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS book_sources (
    bookSourceUrl TEXT PRIMARY KEY,
    bookSourceName TEXT NOT NULL DEFAULT '',
    bookSourceGroup TEXT NOT NULL DEFAULT '',
    bookSourceType INTEGER NOT NULL DEFAULT 0,
    bookUrlPattern TEXT NOT NULL DEFAULT '',
    customOrder INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    enabledExplore INTEGER NOT NULL DEFAULT 0,
    jsLib TEXT NOT NULL DEFAULT '',
    enabledCookieJar INTEGER NOT NULL DEFAULT 1,
    concurrentRate TEXT NOT NULL DEFAULT '',
    header TEXT NOT NULL DEFAULT '{}',
    loginUrl TEXT NOT NULL DEFAULT '',
    loginUi TEXT NOT NULL DEFAULT '',
    loginCheckJs TEXT NOT NULL DEFAULT '',
    coverDecodeJs TEXT NOT NULL DEFAULT '',
    bookSourceComment TEXT NOT NULL DEFAULT '',
    variableComment TEXT NOT NULL DEFAULT '',
    lastUpdateTime INTEGER NOT NULL DEFAULT 0,
    respondTime INTEGER NOT NULL DEFAULT 0,
    weight INTEGER NOT NULL DEFAULT 0,
    exploreUrl TEXT NOT NULL DEFAULT '',
    exploreScreen TEXT NOT NULL DEFAULT '',
    ruleExplore TEXT NOT NULL DEFAULT '{}',
    searchUrl TEXT NOT NULL DEFAULT '',
    ruleSearch TEXT NOT NULL DEFAULT '{}',
    ruleBookInfo TEXT NOT NULL DEFAULT '{}',
    ruleToc TEXT NOT NULL DEFAULT '{}',
    ruleContent TEXT NOT NULL DEFAULT '{}',
    eventListener INTEGER NOT NULL DEFAULT 0,
    customButton INTEGER NOT NULL DEFAULT 0,
    ruleReview TEXT NOT NULL DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS chapters (
    url TEXT NOT NULL,
    bookUrl TEXT NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    isVolume INTEGER NOT NULL DEFAULT 0,
    baseUrl TEXT NOT NULL DEFAULT '',
    index_ INTEGER NOT NULL DEFAULT 0,
    isVip INTEGER NOT NULL DEFAULT 0,
    isPay INTEGER NOT NULL DEFAULT 0,
    resourceUrl TEXT NOT NULL DEFAULT '',
    tag TEXT NOT NULL DEFAULT '',
    wordCount TEXT NOT NULL DEFAULT '',
    start_ INTEGER NOT NULL DEFAULT 0,
    end_ INTEGER NOT NULL DEFAULT 0,
    startFragmentId TEXT NOT NULL DEFAULT '',
    endFragmentId TEXT NOT NULL DEFAULT '',
    variable TEXT NOT NULL DEFAULT '{}',
    PRIMARY KEY (url, bookUrl)
);

CREATE TABLE IF NOT EXISTS rss_sources (
    sourceUrl TEXT PRIMARY KEY,
    sourceName TEXT NOT NULL DEFAULT '',
    sourceIcon TEXT NOT NULL DEFAULT '',
    sourceGroup TEXT NOT NULL DEFAULT '',
    sourceComment TEXT NOT NULL DEFAULT '',
    enabled INTEGER NOT NULL DEFAULT 1,
    variableComment TEXT NOT NULL DEFAULT '',
    jsLib TEXT NOT NULL DEFAULT '',
    enabledCookieJar INTEGER NOT NULL DEFAULT 1,
    concurrentRate TEXT NOT NULL DEFAULT '',
    header TEXT NOT NULL DEFAULT '{}',
    loginUrl TEXT NOT NULL DEFAULT '',
    loginUi TEXT NOT NULL DEFAULT '',
    loginCheckJs TEXT NOT NULL DEFAULT '',
    coverDecodeJs TEXT NOT NULL DEFAULT '',
    sortUrl TEXT NOT NULL DEFAULT '',
    singleUrl INTEGER NOT NULL DEFAULT 0,
    articleStyle INTEGER NOT NULL DEFAULT 0,
    ruleArticles TEXT NOT NULL DEFAULT '',
    ruleNextPage TEXT NOT NULL DEFAULT '',
    ruleTitle TEXT NOT NULL DEFAULT '',
    rulePubDate TEXT NOT NULL DEFAULT '',
    ruleDescription TEXT NOT NULL DEFAULT '',
    ruleImage TEXT NOT NULL DEFAULT '',
    ruleLink TEXT NOT NULL DEFAULT '',
    ruleContent TEXT NOT NULL DEFAULT '',
    contentWhitelist TEXT NOT NULL DEFAULT '',
    contentBlacklist TEXT NOT NULL DEFAULT '',
    shouldOverrideUrlLoading TEXT NOT NULL DEFAULT '',
    style TEXT NOT NULL DEFAULT '',
    enableJs INTEGER NOT NULL DEFAULT 0,
    loadWithBaseUrl INTEGER NOT NULL DEFAULT 0,
    injectJs TEXT NOT NULL DEFAULT '',
    lastUpdateTime INTEGER NOT NULL DEFAULT 0,
    customOrder INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS replace_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL DEFAULT '',
    group_ TEXT NOT NULL DEFAULT '',
    pattern TEXT NOT NULL DEFAULT '',
    replacement TEXT NOT NULL DEFAULT '',
    scope TEXT NOT NULL DEFAULT '',
    scopeTitle INTEGER NOT NULL DEFAULT 0,
    scopeContent INTEGER NOT NULL DEFAULT 0,
    excludeScope TEXT NOT NULL DEFAULT '',
    isEnabled INTEGER NOT NULL DEFAULT 1,
    isRegex INTEGER NOT NULL DEFAULT 0,
    timeoutMillisecond INTEGER NOT NULL DEFAULT 0,
    sortOrder INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS caches (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL DEFAULT '',
    deadline INTEGER NOT NULL DEFAULT 0
);
```

**Step 5.3: BookDao.kt — 书籍数据访问**

```kotlin
package io.legado.server.db.dao

import io.legado.server.db.Database
import io.legado.server.model.entity.Book

object BookDao {
    fun findAll(): List<Book> {
        val conn = Database.getConnection()
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM books ORDER BY order_ ASC")
        val list = mutableListOf<Book>()
        while (rs.next()) {
            list.add(mapRow(rs))
        }
        rs.close()
        stmt.close()
        return list
    }

    fun findByUrl(bookUrl: String): Book? {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("SELECT * FROM books WHERE bookUrl = ?")
        ps.setString(1, bookUrl)
        val rs = ps.executeQuery()
        val book = if (rs.next()) mapRow(rs) else null
        rs.close()
        ps.close()
        return book
    }

    fun save(book: Book) {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("""
            INSERT OR REPLACE INTO books VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent())
        // ... 30 个参数绑定
        ps.setString(1, book.bookUrl)
        ps.setString(2, book.name)
        ps.setString(3, book.author)
        // ... 完整字段映射
        ps.executeUpdate()
        ps.close()
    }

    fun delete(bookUrl: String) {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("DELETE FROM books WHERE bookUrl = ?")
        ps.setString(1, bookUrl)
        ps.executeUpdate()
        ps.close()
    }

    private fun mapRow(rs: java.sql.ResultSet): Book = Book(
        bookUrl = rs.getString("bookUrl") ?: "",
        name = rs.getString("name") ?: "",
        author = rs.getString("author") ?: "",
        coverUrl = rs.getString("coverUrl") ?: "",
        // ... 全部字段
    )
}
```

**Step 5.4: CacheDao.kt — 简单 KV 缓存**

```kotlin
package io.legado.server.db.dao

import io.legado.server.db.Database

object CacheDao {
    fun get(key: String): String? {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("SELECT value, deadline FROM caches WHERE key = ?")
        ps.setString(1, key)
        val rs = ps.executeQuery()
        return if (rs.next()) {
            val deadline = rs.getLong("deadline")
            if (deadline == 0L || deadline > System.currentTimeMillis()) rs.getString("value") else null
        } else null.also { rs.close(); ps.close() }
    }

    fun put(key: String, value: String) {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("INSERT OR REPLACE INTO caches VALUES (?, ?, 0)")
        ps.setString(1, key)
        ps.setString(2, value)
        ps.executeUpdate()
        ps.close()
    }

    fun delete(key: String) {
        val conn = Database.getConnection()
        val ps = conn.prepareStatement("DELETE FROM caches WHERE key = ?")
        ps.setString(1, key)
        ps.executeUpdate()
        ps.close()
    }
}
```

其余 DAO（BookSourceDao、ChapterDao、RssSourceDao、ReplaceRuleDao）结构类似，按对应表字段实现 `findAll`、`findByUrl`、`save`、`delete` 方法。

---

### Task 6: 复制核心分析引擎

**文件：**
- 复制: `legadoT/app/src/main/java/io/legado/app/model/analyzeRule/` → `server/.../analyzeRule/`
- 复制: `legadoT/app/src/main/java/io/legado/app/model/webBook/` → `server/.../webBook/`
- 复制: `legadoT/app/src/main/java/io/legado/app/help/http/` → `server/.../http/`
- 复制: `legadoT/app/src/main/java/io/legado/app/help/rhino/` → `server/.../rhino/`
- 复制: `legadoT/app/src/main/java/io/legado/app/help/JsExtensions.kt` → `server/.../http/JsExtensions.kt`
- 复制: `legadoT/app/src/main/java/io/legado/app/model/SharedJsScope.kt` → `server/.../rhino/`
- 复制: `legadoT/app/src/main/java/io/legado/app/utils/` 部分文件 → `server/.../utils/`

**Step 6.1: 识别无 Android 依赖可直接复制的文件**

```bash
SRC=/home/taole/projects/legadoT/app/src/main/java/io/legado/app
DST=/home/taole/projects/yuedu-web/server/src/main/kotlin/io/legado/server

# 规则引擎 - 纯 Kotlin，无 Android 依赖
cp -r $SRC/model/analyzeRule/*.kt $DST/analyzeRule/
cp -r $SRC/model/analyzeRule/*.java $DST/analyzeRule/

# webBook - 核心爬虫逻辑
mkdir -p $DST/webBook
cp $SRC/model/webBook/WebBook.kt $DST/webBook/
cp $SRC/model/webBook/BookContent.kt $DST/webBook/
cp $SRC/model/webBook/BookChapterList.kt $DST/webBook/
cp $SRC/model/webBook/BookInfo.kt $DST/webBook/
cp $SRC/model/webBook/BookList.kt $DST/webBook/
cp $SRC/model/webBook/SearchModel.kt $DST/webBook/

# 工具类
mkdir -p $DST/utils
cp $SRC/utils/GsonExtensions.kt $DST/utils/
cp $SRC/utils/StringExtensions.kt $DST/utils/
cp $SRC/utils/RegexExtensions.kt $DST/utils/
cp $SRC/utils/NetworkUtils.kt $DST/utils/
cp $SRC/utils/UrlUtil.kt $DST/utils/
```

**Step 6.2: 修改依赖了 Android API 的文件**

需要逐文件检查并修改的 Android 依赖：

| Android API | 替换方案 |
|-------------|---------|
| `android.content.Context` | 参数改为 `String` 路径或删除 |
| `android.text.TextUtils.isEmpty()` | `str.isNullOrEmpty()` |
| `android.util.Base64` | `java.util.Base64` |
| `android.net.Uri` | `java.net.URI` |
| `android.util.Patterns` | 正则直接硬编码 |
| `splitties.init.appCtx` | 替换为静态变量 |
| `BuildConfig.DEBUG` | 系统属性或常量 |
| `LogUtils` / `android.util.Log` | SLF4J (logback) |
| `android.graphics.Bitmap` / `BitmapFactory` | `java.awt.image.BufferedImage` / `ImageIO` |
| `ContextCompat` / `PermissionCompat` | 移除 |

**Step 6.3: 修改 HttpHelper.kt**

原文件使用 `appCtx` 获取缓存目录等。改为传参：

```kotlin
// 在类中新增
class HttpHelper(private val cacheDir: String? = null) {
    // 将 appCtx.cacheDir 替换为 cacheDir
}
```

**Step 6.4: 修改 SharedJsScope.kt**

移除 Android 相关 import，将 `getCryptoScope()` 中加载的 JS 文件改为从 classpath 读取：

```kotlin
private fun loadJs(name: String): String {
    return javaClass.getResourceAsStream("/js/$name")?.bufferedReader()?.readText()
        ?: throw IllegalStateException("JS file not found: $name")
}
```

将 crypto JS 文件放入 `server/src/main/resources/js/`。

---

### Task 7: 实现图片代理

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/image/ImageProxy.kt`
- 创建: `server/src/main/kotlin/io/legado/server/image/ImageCache.kt`

**Step 7.1: ImageCache.kt — 图片文件缓存**

```kotlin
package io.legado.server.image

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

class ImageCache(private val cacheDir: File) {
    init {
        cacheDir.mkdirs()
    }

    private fun hashUrl(url: String): String =
        MessageDigest.getInstance("MD5").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }

    fun get(url: String): ByteArray? {
        val file = File(cacheDir, hashUrl(url))
        return if (file.exists()) FileInputStream(file).readBytes() else null
    }

    fun put(url: String, data: ByteArray) {
        File(cacheDir, hashUrl(url)).outputStream().use { it.write(data) }
    }

    fun getPath(url: String): String = File(cacheDir, hashUrl(url)).absolutePath
}
```

**Step 7.2: ImageProxy.kt — 图片下载+缩放**

```kotlin
package io.legado.server.image

import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

class ImageProxy(
    private val cache: ImageCache,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val logger = LoggerFactory.getLogger(ImageProxy::class.java)

    fun getImage(url: String, maxWidth: Int = 0): ByteArray {
        // 1. 检查缓存
        cache.get(url)?.let { return it }

        // 2. 下载
        val request = okhttp3.Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        val bytes = response.body?.bytes() ?: throw Exception("Empty response")

        // 3. 缩放
        val result = if (maxWidth > 0) {
            val input = ImageIO.read(ByteArrayInputStream(bytes))
            if (input != null && input.width > maxWidth) {
                val ratio = maxWidth.toDouble() / input.width
                val output = BufferedImage(maxWidth, (input.height * ratio).toInt(), BufferedImage.TYPE_INT_RGB)
                output.graphics.drawImage(input, 0, 0, output.width, output.height, null)
                val baos = java.io.ByteArrayOutputStream()
                ImageIO.write(output, "png", baos)
                baos.toByteArray()
            } else bytes
        } else bytes

        // 4. 写入缓存
        cache.put(url, result)
        return result
    }
}
```

---

### Task 8: 复制并改造 API 控制器

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/web/controller/BookController.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/controller/BookSourceController.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/controller/RssSourceController.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/controller/ReplaceRuleController.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/AssetsWeb.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/HttpServer.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/WebSocketServer.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/socket/BookSearchWebSocket.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/socket/BookSourceDebugWebSocket.kt`
- 创建: `server/src/main/kotlin/io/legado/server/web/socket/RssSourceDebugWebSocket.kt`
- 创建: `server/src/main/kotlin/io/legado/server/service/` 业务服务层

**Step 8.1: 改造 HttpServer.kt**

从 legadoT 复制后改造：

```kotlin
package io.legado.server.web

import fi.iki.elonen.NanoHTTPD
import io.legado.server.web.controller.BookController
import io.legado.server.web.controller.BookSourceController
import io.legado.server.web.controller.ReplaceRuleController
import io.legado.server.web.controller.RssSourceController
import org.slf4j.LoggerFactory
import java.io.File

class HttpServer(
    port: Int,
    private val webRoot: File,
    private val dataDir: File
) : NanoHTTPD(port) {

    private val logger = LoggerFactory.getLogger(HttpServer::class.java)
    private val bookController = BookController(dataDir)
    private val bookSourceController = BookSourceController()
    private val rssSourceController = RssSourceController()
    private val replaceRuleController = ReplaceRuleController()

    override fun serve(session: IHTTPSession): Response {
        return when (session.method) {
            Method.OPTIONS -> handleCors(session)
            Method.GET -> handleGet(session)
            Method.POST -> handlePost(session)
            else -> newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "")
        }
    }

    private fun handleGet(session: IHTTPSession): Response {
        val uri = session.uri
        return when {
            uri == "/getBookshelf" -> bookController.getBookshelf()
            uri.startsWith("/getChapterList") -> bookController.getChapterList(session)
            uri.startsWith("/getBookContent") -> bookController.getBookContent(session)
            uri.startsWith("/cover") -> bookController.getCover(session)
            uri.startsWith("/image") -> bookController.getImage(session)
            uri.startsWith("/getBookSource") -> bookSourceController.getBookSource(session)
            uri == "/getBookSources" -> bookSourceController.getBookSources()
            uri.startsWith("/getRssSource") -> rssSourceController.getRssSource(session)
            uri == "/getRssSources" -> rssSourceController.getRssSources()
            uri == "/getReplaceRules" -> replaceRuleController.getReplaceRules()
            uri == "/getReadConfig" -> bookController.getReadConfig()
            else -> serveStatic(uri)
        }
    }

    private fun handlePost(session: IHTTPSession): Response {
        val uri = session.uri
        return when (uri) {
            "/saveBook" -> bookController.saveBook(session)
            "/deleteBook" -> bookController.deleteBook(session)
            "/saveBookProgress" -> bookController.saveBookProgress(session)
            "/saveBookSource" -> bookSourceController.saveBookSource(session)
            "/saveBookSources" -> bookSourceController.saveBookSources(session)
            "/deleteBookSources" -> bookSourceController.deleteBookSources(session)
            "/saveRssSource" -> rssSourceController.saveRssSource(session)
            "/saveRssSources" -> rssSourceController.saveRssSources(session)
            "/deleteRssSources" -> rssSourceController.deleteRssSources(session)
            "/saveReplaceRule" -> replaceRuleController.saveReplaceRule(session)
            "/deleteReplaceRule" -> replaceRuleController.deleteReplaceRule(session)
            "/testReplaceRule" -> replaceRuleController.testReplaceRule(session)
            "/saveReadConfig" -> bookController.saveReadConfig(session)
            "/refreshToc" -> bookController.refreshToc(session)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "")
        }
    }

    private fun handleCors(session: IHTTPSession): Response {
        val response = newFixedLengthResponse(Response.Status.OK, "text/plain", "")
        response.addHeader("Access-Control-Allow-Methods", "POST, GET")
        response.addHeader("Access-Control-Allow-Headers", "content-type")
        response.addHeader("Access-Control-Allow-Origin", session.headers["origin"] ?: "*")
        return response
    }

    private fun serveStatic(uri: String): Response {
        // / 和 /index.html 都指向 web/vue/index.html
        val path = when {
            uri == "/" || uri == "" -> "/vue/index.html"
            uri.startsWith("/vue/") || uri.startsWith("/assets/") -> uri
            else -> "/vue/index.html"  // SPA fallback
        }
        val file = File(webRoot, path)
        if (file.exists()) {
            val mime = getMimeType(path)
            return newChunkedResponse(Response.Status.OK, mime, file.inputStream())
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404")
    }

    private fun getMimeType(path: String): String = when {
        path.endsWith(".html") -> "text/html; charset=utf-8"
        path.endsWith(".js") -> "text/javascript; charset=utf-8"
        path.endsWith(".css") -> "text/css"
        path.endsWith(".png") -> "image/png"
        path.endsWith(".svg") -> "image/svg+xml"
        path.endsWith(".woff") -> "font/woff"
        path.endsWith(".woff2") -> "font/woff2"
        path.endsWith(".ttf") -> "font/ttf"
        path.endsWith(".ico") -> "image/x-icon"
        else -> "text/plain"
    }
}
```

**Step 8.2: 改造 BookController.kt（核心 API）**

从 `legadoT/app/src/main/java/io/legado/app/api/controller/BookController.kt` 复制改造：

```kotlin
package io.legado.server.web.controller

import fi.iki.elonen.NanoHTTPD
import io.legado.server.db.dao.BookDao
import io.legado.server.db.dao.ChapterDao
import io.legado.server.db.dao.CacheDao
import io.legado.server.image.ImageProxy
import io.legado.server.model.entity.Book
import io.legado.server.service.ContentService
import com.google.gson.Gson

class BookController(private val dataDir: java.io.File) {
    private val gson = Gson()
    private val imageProxy = ImageProxy(
        io.legado.server.image.ImageCache(java.io.File(dataDir, "images"))
    )
    private val contentService = ContentService(dataDir)

    fun getBookshelf(): NanoHTTPD.Response = jsonResponse(mapOf(
        "isSuccess" to true,
        "data" to BookDao.findAll()
    ))

    fun getChapterList(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val bookUrl = session.parameters["url"]?.firstOrNull() ?: return errorResponse("Missing url")
        val chapters = ChapterDao.findByBookUrl(bookUrl)
        return jsonResponse(mapOf("isSuccess" to true, "data" to chapters))
    }

    fun getBookContent(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val bookUrl = session.parameters["url"]?.firstOrNull() ?: return errorResponse("Missing url")
        val index = session.parameters["index"]?.firstOrNull()?.toIntOrNull() ?: return errorResponse("Missing index")
        val content = contentService.getContent(bookUrl, index)
        return jsonResponse(mapOf("isSuccess" to true, "data" to content))
    }

    fun saveBook(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val body = parseBody(session)
        val book = gson.fromJson(body, Book::class.java)
        BookDao.save(book)
        return jsonResponse(mapOf("isSuccess" to true))
    }

    fun deleteBook(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val body = parseBody(session)
        val map = gson.fromJson(body, Map::class.java)
        val bookUrl = map["bookUrl"] as? String ?: return errorResponse("Missing bookUrl")
        BookDao.delete(bookUrl)
        return jsonResponse(mapOf("isSuccess" to true))
    }

    fun saveBookProgress(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val body = parseBody(session)
        val map = gson.fromJson(body, Map::class.java)
        val bookUrl = map["bookUrl"] as? String ?: return errorResponse("Missing bookUrl")
        val book = BookDao.findByUrl(bookUrl) ?: return errorResponse("Book not found")
        val updated = book.copy(
            durChapterIndex = (map["durChapterIndex"] as? Double)?.toInt() ?: book.durChapterIndex,
            durChapterPos = (map["durChapterPos"] as? Double)?.toInt() ?: book.durChapterPos,
            durChapterTitle = map["durChapterTitle"] as? String ?: book.durChapterTitle,
            durChapterTime = (map["durChapterTime"] as? Double)?.toLong() ?: System.currentTimeMillis()
        )
        BookDao.save(updated)
        return jsonResponse(mapOf("isSuccess" to true))
    }

    fun getCover(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val path = session.parameters["path"]?.firstOrNull() ?: return errorResponse("Missing path")
        val bytes = try {
            imageProxy.getImage(path, 84)
        } catch (e: Exception) {
            // 返回默认图片
            return defaultCoverResponse()
        }
        return NanoHTTPD.newChunkedResponse(
            NanoHTTPD.Response.Status.OK, "image/png", bytes.inputStream()
        ).also { it.addHeader("Cache-Control", "max-age=86400") }
    }

    fun getImage(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val path = session.parameters["path"]?.firstOrNull() ?: return errorResponse("Missing path")
        val width = session.parameters["width"]?.firstOrNull()?.toIntOrNull() ?: 0
        val bytes = imageProxy.getImage(path, width)
        return NanoHTTPD.newChunkedResponse(
            NanoHTTPD.Response.Status.OK, "image/png", bytes.inputStream()
        ).also { it.addHeader("Cache-Control", "max-age=86400") }
    }

    fun getReadConfig(): NanoHTTPD.Response {
        val config = CacheDao.get("webReadConfig") ?: "{}"
        return jsonResponse(mapOf("isSuccess" to true, "data" to config))
    }

    fun saveReadConfig(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val body = parseBody(session)
        CacheDao.put("webReadConfig", body)
        return jsonResponse(mapOf("isSuccess" to true))
    }

    fun refreshToc(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val bookUrl = session.parameters["url"]?.firstOrNull() ?: return errorResponse("Missing url")
        val chapters = contentService.refreshToc(bookUrl)
        return jsonResponse(mapOf("isSuccess" to true, "data" to chapters))
    }

    // 辅助方法
    private fun jsonResponse(data: Map<*, *>): NanoHTTPD.Response =
        NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json; charset=utf-8", gson.toJson(data))

    private fun errorResponse(msg: String): NanoHTTPD.Response =
        jsonResponse(mapOf("isSuccess" to false, "errorMsg" to msg))

    private fun defaultCoverResponse(): NanoHTTPD.Response {
        // 返回 1x1 PNG 占位图
        val emptyPng = java.io.ByteArrayOutputStream()
        javax.imageio.ImageIO.write(
            java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB),
            "png", emptyPng
        )
        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/png", java.io.ByteArrayInputStream(emptyPng.toByteArray()))
    }

    private fun parseBody(session: NanoHTTPD.IHTTPSession): String {
        val files = HashMap<String, String>()
        session.parseBody(files)
        return files["postData"] ?: ""
    }
}
```

---

### Task 9: 实现内容获取服务

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/service/ContentService.kt`
- 创建: `server/src/main/kotlin/io/legado/server/service/BookService.kt`
- 创建: `server/src/main/kotlin/io/legado/server/service/SourceService.kt`
- 创建: `server/src/main/kotlin/io/legado/server/service/DebugService.kt`
- 创建: `server/src/main/kotlin/io/legado/server/service/SearchService.kt`

**Step 9.1: ContentService.kt — 内容获取+缓存**

```kotlin
package io.legado.server.service

import io.legado.server.analyzeRule.AnalyzeRule
import io.legado.server.db.dao.BookDao
import io.legado.server.db.dao.ChapterDao
import io.legado.server.http.HttpHelper
import io.legado.server.model.entity.BookChapter
import io.legado.server.model.entity.BookSource
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ContentService(private val dataDir: File) {
    private val logger = LoggerFactory.getLogger(ContentService::class.java)
    private val httpClient = OkHttpClient()
    private val contentCacheDir = File(dataDir, "contents").also { it.mkdirs() }
    private val httpHelper = HttpHelper(dataDir.absolutePath)

    /**
     * 获取章节内容：优先读本地缓存，否则从网络抓取
     */
    fun getContent(bookUrl: String, chapterIndex: Int): String {
        // 1. 尝试读缓存
        val cacheFile = File(contentCacheDir, "${bookUrl.hashCode()}_$chapterIndex")
        if (cacheFile.exists()) {
            return cacheFile.readText()
        }

        // 2. 查找章节信息
        val chapter = ChapterDao.findByBookUrlAndIndex(bookUrl, chapterIndex)
            ?: return "章节信息不存在"

        // 3. 查找书源
        val book = BookDao.findByUrl(bookUrl) ?: return "书籍不存在"
        val source = io.legado.server.db.dao.BookSourceDao.findByUrl(book.origin)
            ?: return "书源不存在"

        // 4. 通过网络抓取
        val fetcher = io.legado.server.webBook.BookContent()
        val content = fetcher.getContent(source, book, chapter, httpHelper)

        // 5. 写入缓存
        cacheFile.writeText(content)
        return content
    }

    fun refreshToc(bookUrl: String): List<BookChapter> {
        val book = BookDao.findByUrl(bookUrl) ?: return emptyList()
        val source = io.legado.server.db.dao.BookSourceDao.findByUrl(book.origin) ?: return emptyList()

        val fetcher = io.legado.server.webBook.BookChapterList()
        val chapters = fetcher.getChapterList(source, book, httpHelper)

        chapters.forEach { ChapterDao.save(it) }
        return chapters
    }
}
```

---

### Task 10: 实现 WebSocket 端点

**Step 10.1: WebSocketServer.kt**

```kotlin
package io.legado.server.web

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import io.legado.server.web.socket.BookSearchWebSocket
import io.legado.server.web.socket.BookSourceDebugWebSocket
import io.legado.server.web.socket.RssSourceDebugWebSocket
import org.slf4j.LoggerFactory
import java.io.File

class WebSocketServer(port: Int, private val dataDir: File) : NanoWSD(port) {
    private val logger = LoggerFactory.getLogger(WebSocketServer::class.java)

    override fun openWebSocket(request: NanoHTTPD.IHTTPSession): WebSocket {
        return when (request.uri) {
            "/searchBook" -> BookSearchWebSocket(request)
            "/bookSourceDebug" -> BookSourceDebugWebSocket(request, dataDir)
            "/rssSourceDebug" -> RssSourceDebugWebSocket(request, dataDir)
            else -> throw Exception("Unknown WebSocket endpoint: ${request.uri}")
        }
    }
}
```

**Step 10.2: BookSearchWebSocket.kt**

```kotlin
package io.legado.server.web.socket

import fi.iki.elonen.NanoWSD
import io.legado.server.db.dao.BookSourceDao
import io.legado.server.webBook.SearchModel
import org.slf4j.LoggerFactory
import java.io.File

class BookSearchWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
    NanoWSD.WebSocket(handshakeRequest) {

    private val logger = LoggerFactory.getLogger(BookSearchWebSocket::class.java)

    override fun onOpen() {
        setPingInterval(30_000)
    }

    override fun onClose(code: Int, reason: String?, initiatedByRemote: Boolean) {}

    override fun onMessage(frame: NanoWSD.WebSocketFrame) {
        val message = frame.textPayload
        // 解析 {"key": "search term"}
        val map = com.google.gson.Gson().fromJson(message, Map::class.java)
        val key = map["key"] as? String ?: return

        val sources = BookSourceDao.findAllEnabled().filter { it.searchUrl.isNotBlank() }
        val searchModel = SearchModel()

        // 逐源搜索，结果实时返回
        searchModel.search(key, sources) { result ->
            val json = com.google.gson.Gson().toJson(result)
            send(json)
        }

        close(NanoWSD.WebSocketFrame.CLOSE_NORMAL, "done")
    }

    override fun onPong(frame: NanoWSD.WebSocketFrame?) {}
}
```

---

### Task 11: 创建应用入口

**文件：**
- 创建: `server/src/main/kotlin/io/legado/server/App.kt`
- 创建: `server/src/main/kotlin/io/legado/server/config/ServerConfig.kt`
- 创建: `server/src/main/resources/logback.xml`

**Step 11.1: ServerConfig.kt**

```kotlin
package io.legado.server.config

import java.io.File

data class ServerConfig(
    val httpPort: Int = 1122,
    val wsPort: Int = 1123,
    val dataDir: File = File(System.getProperty("user.home"), ".yuedu-web"),
    val webRoot: File = File(System.getProperty("user.dir"), "web")
) {
    init {
        dataDir.mkdirs()
        webRoot.mkdirs()
    }

    companion object {
        fun fromEnv(): ServerConfig {
            val port = System.getenv("YUEDU_PORT")?.toIntOrNull() ?: 1122
            val dataDir = System.getenv("YUEDU_DATA_DIR")?.let { File(it) }
                ?: File(System.getProperty("user.home"), ".yuedu-web")
            val webRoot = System.getenv("YUEDU_WEB_ROOT")?.let { File(it) }
                ?: File(System.getProperty("user.dir"), "web")
            return ServerConfig(httpPort = port, wsPort = port + 1, dataDir = dataDir, webRoot = webRoot)
        }
    }
}
```

**Step 11.2: App.kt — 主入口**

```kotlin
package io.legado.server

import io.legado.server.config.ServerConfig
import io.legado.server.db.Database
import io.legado.server.web.HttpServer
import io.legado.server.web.WebSocketServer
import org.slf4j.LoggerFactory

fun main() {
    val config = ServerConfig.fromEnv()
    val logger = LoggerFactory.getLogger("App")

    logger.info("Starting yuedu-web server...")
    logger.info("  HTTP port: ${config.httpPort}")
    logger.info("  WS port:   ${config.wsPort}")
    logger.info("  Data dir:  ${config.dataDir}")
    logger.info("  Web root:  ${config.webRoot}")

    // 初始化数据库
    Database.init(config.dataDir.absolutePath)

    // 启动 HTTP 服务器
    val httpServer = HttpServer(config.httpPort, config.webRoot, config.dataDir)
    httpServer.start()
    logger.info("HTTP server started on port ${config.httpPort}")

    // 启动 WebSocket 服务器
    val wsServer = WebSocketServer(config.wsPort, config.dataDir)
    wsServer.start()
    logger.info("WebSocket server started on port ${config.wsPort}")

    // 优雅关闭
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Shutting down...")
        httpServer.stop()
        wsServer.stop()
        Database.close()
    })

    println("=".repeat(50))
    println("  yuedu-web server running!")
    println("  Open http://localhost:${config.httpPort} in browser")
    println("=".repeat(50))
}
```

**Step 11.3: logback.xml**

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

### Task 12: 构建 Web 前端并整合

**Step 12.1: 将 Vue 前端构建产物链接到 server**

```bash
cd /home/taole/projects/legadoT/modules/web
pnpm install
pnpm build-only

# 将构建产物复制到 yuedu-web 的 web 目录
mkdir -p /home/taole/projects/yuedu-web/web/vue
cp -r dist/* /home/taole/projects/yuedu-web/web/vue/
```

或者在 `yuedu-web/build.gradle.kts` 中配置 Gradle task 自动化此步骤。

**Step 12.2: 验证前端路由兼容性**

前端代码中图片路径使用的是 `/cover?path=...` 和 `/image?url=...&path=...` 格式，这些已在 HttpServer 中实现。需要确认：

- `modules/web/src/utils/index.ts` 中 `getProxyCoverUrl` 函数是否使用相对路径
- 如果前端构建时 `base: "./"`（已在 vite.config.ts 中设置），则静态资源路径正确

---

## 实施顺序

```
Task 1  — 项目脚手架
   │
Task 2  — modules:rhino (纯 JVM)
   │
Task 3  — modules:book (纯 JVM)
   │
Task 4  — 实体类
   │
Task 5  — 数据库层
   │
Task 6  — 核心引擎复制
   │
Task 7  — 图片代理
   │
Task 8  — HTTP 服务器 + API 控制器
   │
Task 9  — 内容获取服务
   │
Task 10 — WebSocket 端点
   │
Task 11 — 应用入口
   │
Task 12 — 构建前端 + 整合
```

每个 Task 完成后可以 `./gradlew :server:jar` 打包验证。建议每完成 2-3 个 Task 就做一次集成测试。

---

## 测试验证

每个 Task 完成后运行：

```bash
# 编译所有模块
./gradlew build

# 打包可执行 JAR
./gradlew :server:jar

# 启动服务器
java -jar server/build/libs/server-1.0.0.jar

# 在浏览器访问
open http://localhost:1122
```

启动后可验证：
- [ ] 浏览器打开后显示书架页面（可能为空）
- [ ] API 端点返回正常 JSON：`curl http://localhost:1122/getBookshelf`
- [ ] WebSocket 连接正常：`websocat ws://localhost:1123/searchBook`
- [ ] 导入书源后能搜索
- [ ] 添加书籍后能阅读

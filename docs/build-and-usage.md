# yuedu-web 构建及使用手册

## 项目简介

yuedu-web 是 [Legado (阅读T)](https://github.com/feilewu/legadoT) 的独立 Web 服务端，将原 Android 应用的核心爬虫引擎（AnalyzeRule + Rhino JS + OkHttp）提取为纯 JVM 服务端，使 Web 前端可以脱离手机运行。

**功能**: 书籍搜索、书架管理、在线阅读、书源编辑、RSS 订阅。

---

## 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 17+ (推荐 21) |
| Gradle | 8.12 (wrapper 会自动下载) |
| Node.js | 20+ (仅构建前端需要) |
| pnpm | 9+ (仅构建前端需要) |

---

## 构建

### 1. 构建服务端

```bash
cd yuedu-web

# 编译并打包为可执行 JAR
./gradlew :server:jar
```

构建产物: `server/build/libs/server-1.0.0.jar`

### 2. 构建 Web 前端

前端源码在 `frontend/` 目录中。

```bash
cd yuedu-web/frontend

# 安装依赖
pnpm install

# 构建并复制到 web/vue/
pnpm build
```

构建完成后产物自动复制到 `web/vue/`，可直接启动服务。

---

## 使用

### 启动服务

```bash
cd yuedu-web
java --add-opens java.base/java.time=ALL-UNNAMED \
     -Dyuedu.web.root=$(pwd) \
     -jar server/build/libs/server-1.0.0.jar
```

启动后输出:
```
Starting yuedu-web...
  HTTP port: 1122
  Data dir:  /home/user/.yuedu-web
  Web root:  /path/to/yuedu-web/web
HTTP server started on port 1122
WebSocket server started on port 1123
==================================================
  Open http://localhost:1122 in browser
==================================================
```

### 配置项

| 方式 | 配置项 | 说明 | 默认值 |
|------|--------|------|--------|
| 环境变量 | `YUEDU_PORT` | HTTP 服务端口 | 1122 |
| 环境变量 | `YUEDU_DATA_DIR` | 数据目录（数据库） | `~/.yuedu-web` |
| 环境变量 | `YUEDU_WEB_ROOT` | Web 前端静态文件目录 | `$(pwd)/web` |
| JVM 属性 | `yuedu.web.root` | Web 前端根目录 | `user.dir` |
| JVM 参数 | `--add-opens java.base/java.time=ALL-UNNAMED` | Gson 序列化 LocalDate 兼容 | 必须 |

WebSocket 端口固定为 HTTP 端口 + 1。

### 导入书源

使用附带的 Python 脚本导入书源：

```bash
# 从 URL 导入
python3 scripts/import-sources.py https://raw.githubusercontent.com/XIU2/Yuedu/master/shuyuan

# 从本地 JSON 文件导入
python3 scripts/import-sources.py /path/to/sources.json
```

脚本会自动将 JSON 数组中的书源写入 SQLite 数据库，重复导入会自动更新。

---

## 架构说明

```
┌─ 用户浏览器 ─────────────────────┐
│  Vue 3 SPA (modules/web/)        │
│  http://localhost:1122            │
└──────────┬───────────────────────┘
           │ HTTP REST + WebSocket
           ▼
┌─ yuedu-web 服务端 ────────────────┐
│  NanoHTTPD (端口 1122)            │
│    ├─ REST API (书架/搜索/阅读)   │
│    └─ 静态文件服务 (web/vue/)      │
│  NanoWSD (端口 1123)              │
│    ├─ /searchBook (多源搜索)       │
│    ├─ /bookSourceDebug             │
│    └─ /rssSourceDebug              │
│  SQLite (书库/书源/配置)           │
└──────────────────────────────────┘
```

### 核心模块

| 模块 | 说明 |
|------|------|
| `server/` | 主服务：HTTP 服务器、API 控制器、WebSocket、爬虫引擎 |
| `modules/rhino/` | Rhino JS 引擎封装（htmlunit-core-js），用于执行书源 JS 规则 |
| `modules/book/` | EPUB/UMD 电子书格式解析库 |

### 技术栈

- **Kotlin 2.3.0** + **JVM 17+** — 服务端语言
- **NanoHTTPD 2.3.1** — 嵌入式 HTTP 服务器 + WebSocket
- **OkHttp 5.x** — HTTP 客户端
- **htmlunit-core-js 5.0.0** — Rhino JS 引擎
- **SQLite (JDBC)** — 数据库
- **Gson** — JSON 序列化
- **JSoup 1.16.2** — HTML 解析
- **Logback** — 日志
- **Vue 3** — Web 前端（独立项目）

### REST API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getBookshelf` | 获取书架列表 |
| GET | `/getChapterList?url=` | 获取章节列表 |
| GET | `/getBookContent?url=&index=` | 获取章节正文 |
| GET | `/getBookSources` | 获取书源列表 |
| GET | `/getBookSource?url=` | 获取单个书源 |
| GET | `/getRssSources` | 获取 RSS 源列表 |
| GET | `/getRssSource?url=` | 获取单个 RSS 源 |
| GET | `/getReplaceRules` | 获取替换规则列表 |
| GET | `/getReadConfig` | 获取阅读配置 |
| GET | `/cover?path=` | 封面图片代理 |
| GET | `/image?url=&path=&width=` | 正文图片代理 |
| GET | `/refreshToc?url=` | 刷新目录 |
| POST | `/saveBook` | 保存书籍到书架 |
| POST | `/deleteBook` | 从书架删除书籍 |
| POST | `/saveBookProgress` | 保存阅读进度 |
| POST | `/saveBookSource` | 保存书源 |
| POST | `/saveBookSources` | 批量保存书源 |
| POST | `/deleteBookSources` | 删除书源 |
| POST | `/saveRssSource` | 保存 RSS 源 |
| POST | `/saveRssSources` | 批量保存 RSS 源 |
| POST | `/deleteRssSources` | 删除 RSS 源 |
| POST | `/saveReplaceRule` | 保存替换规则 |
| POST | `/deleteReplaceRule` | 删除替换规则 |
| POST | `/testReplaceRule` | 测试替换规则 |
| POST | `/saveReadConfig` | 保存阅读配置 |

所有返回格式：`{"isSuccess": true/false, "errorMsg": "..." , "data": ...}`

---

## 常见问题

**Q: 中文显示乱码？**
A: 确保启动时 Java 环境为 UTF-8。确认 `file.encoding=UTF-8`，POST 请求的 Content-Type 需包含 `charset=utf-8`。

**Q: 端口被占用？**
A: 设置环境变量 `YUEDU_PORT=2222` 使用其他端口。

**Q: Web 前端页面空白？**
A: 确认 `web/vue/index.html` 存在。如果不存在，需要先构建前端（见上方"构建 Web 前端"章节）。

**Q: 如何停止服务？**
A: `Ctrl+C` 或 `kill <PID>`。

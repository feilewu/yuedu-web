package io.legado.server.web.controller

import com.google.gson.reflect.TypeToken
import io.legado.server.db.dao.BookSourceDao
import io.legado.server.utils.GSON
import io.legado.server.model.entity.BookSource
import io.legado.server.web.ReturnData

object BookSourceController {

    fun getSources(): ReturnData {
        val sources = BookSourceDao.findAll()
        return if (sources.isEmpty()) {
            ReturnData.error("书源列表为空")
        } else {
            ReturnData.success(sources)
        }
    }

    fun getSource(parameters: Map<String, List<String>>): ReturnData {
        val url = parameters["url"]?.firstOrNull()
        if (url.isNullOrEmpty()) {
            return ReturnData.error("参数url不能为空，请指定书源地址")
        }
        val source = BookSourceDao.findByUrl(url)
        if (source == null) {
            return ReturnData.error("未找到书源，请检查书源地址")
        }
        return ReturnData.success(source)
    }

    fun saveSource(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val source = GSON.fromJson(postData, BookSource::class.java)
            if (source.bookSourceName.isBlank() || source.bookSourceUrl.isBlank()) {
                ReturnData.error("书源名称和URL不能为空")
            } else {
                BookSourceDao.save(source)
                ReturnData.success("")
            }
        } catch (e: Exception) {
            ReturnData.error("转换书源失败: ${e.message}")
        }
    }

    fun saveSources(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据为空")
        return try {
            val sources: List<BookSource> = GSON.fromJson(postData, object : TypeToken<List<BookSource>>() {}.type)
            val okSources = mutableListOf<BookSource>()
            for (source in sources) {
                if (source.bookSourceName.isNotBlank() && source.bookSourceUrl.isNotBlank()) {
                    BookSourceDao.save(source)
                    okSources.add(source)
                }
            }
            ReturnData.success(okSources)
        } catch (e: Exception) {
            ReturnData.error("转换书源失败: ${e.message}")
        }
    }

    fun deleteSources(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("没有传递数据")
        return try {
            val sources: List<BookSource> = GSON.fromJson(postData, object : TypeToken<List<BookSource>>() {}.type)
            for (source in sources) {
                BookSourceDao.delete(source.bookSourceUrl)
            }
            ReturnData.success("已执行")
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }
}

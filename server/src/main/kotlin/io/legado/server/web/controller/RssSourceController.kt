package io.legado.server.web.controller

import com.google.gson.reflect.TypeToken
import io.legado.server.db.dao.RssSourceDao
import io.legado.server.utils.GSON
import io.legado.server.model.entity.RssSource
import io.legado.server.web.ReturnData

object RssSourceController {

    fun getSources(): ReturnData {
        val sources = RssSourceDao.findAll()
        return if (sources.isEmpty()) {
            ReturnData.error("订阅源列表为空")
        } else {
            ReturnData.success(sources)
        }
    }

    fun getSource(parameters: Map<String, List<String>>): ReturnData {
        val url = parameters["url"]?.firstOrNull()
        if (url.isNullOrEmpty()) {
            return ReturnData.error("参数url不能为空，请指定订阅源地址")
        }
        val source = RssSourceDao.findByUrl(url)
        if (source == null) {
            return ReturnData.error("未找到订阅源，请检查订阅源地址")
        }
        return ReturnData.success(source)
    }

    fun saveSource(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val source = GSON.fromJson(postData, RssSource::class.java)
            if (source.sourceName.isBlank() || source.sourceUrl.isBlank()) {
                ReturnData.error("订阅源名称和URL不能为空")
            } else {
                RssSourceDao.save(source)
                ReturnData.success("")
            }
        } catch (e: Exception) {
            ReturnData.error("转换订阅源失败: ${e.message}")
        }
    }

    fun saveSources(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val sources: List<RssSource> = GSON.fromJson(postData, object : TypeToken<List<RssSource>>() {}.type)
            val okSources = mutableListOf<RssSource>()
            for (source in sources) {
                if (source.sourceName.isNotBlank() && source.sourceUrl.isNotBlank()) {
                    RssSourceDao.save(source)
                    okSources.add(source)
                }
            }
            ReturnData.success(okSources)
        } catch (e: Exception) {
            ReturnData.error("转换订阅源失败: ${e.message}")
        }
    }

    fun deleteSources(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("没有传递数据")
        return try {
            val sources: List<RssSource> = GSON.fromJson(postData, object : TypeToken<List<RssSource>>() {}.type)
            for (source in sources) {
                RssSourceDao.delete(source.sourceUrl)
            }
            ReturnData.success("已执行")
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }
}

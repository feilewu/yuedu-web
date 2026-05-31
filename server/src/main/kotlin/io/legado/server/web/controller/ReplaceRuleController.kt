package io.legado.server.web.controller

import com.google.gson.reflect.TypeToken
import io.legado.server.db.dao.ReplaceRuleDao
import io.legado.server.utils.GSON
import io.legado.server.model.entity.ReplaceRule
import io.legado.server.web.ReturnData

object ReplaceRuleController {

    fun getAllRules(): ReturnData {
        val rules = ReplaceRuleDao.findAll()
        return ReturnData.success(GSON.toJson(rules))
    }

    fun saveRule(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val rule = GSON.fromJson(postData, ReplaceRule::class.java)
            ReplaceRuleDao.save(rule)
            ReturnData.success("")
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }

    fun delete(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val rule = GSON.fromJson(postData, ReplaceRule::class.java)
            ReplaceRuleDao.delete(rule)
            ReturnData.success("")
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }

    fun testRule(postData: String?): ReturnData {
        if (postData.isNullOrBlank()) return ReturnData.error("数据不能为空")
        return try {
            val mapType = object : TypeToken<Map<String, *>>() {}.type
            val map: Map<String, *> = GSON.fromJson(postData, mapType)
            val ruleObj = map["rule"]
            val rule: ReplaceRule = when (ruleObj) {
                is String -> GSON.fromJson(ruleObj, ReplaceRule::class.java)
                else -> GSON.fromJson(GSON.toJson(ruleObj), ReplaceRule::class.java)
            }
            if (rule.pattern.isEmpty()) {
                return ReturnData.error("替换规则不能为空")
            }
            val text = map["text"] as? String ?: return ReturnData.error("text不能为空")
            val content = try {
                if (rule.isRegex) {
                    text.replace(Regex(rule.pattern), rule.replacement)
                } else {
                    text.replace(rule.pattern, rule.replacement)
                }
            } catch (e: Exception) {
                e.stackTraceToString()
            }
            ReturnData.success(content)
        } catch (e: Exception) {
            ReturnData.error("格式不对: ${e.message}")
        }
    }
}

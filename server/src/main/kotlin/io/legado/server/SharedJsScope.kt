package io.legado.server

import com.script.ScriptBindings
import com.script.rhino.RhinoScriptEngine
import org.htmlunit.corejs.javascript.Scriptable
import org.htmlunit.corejs.javascript.ScriptableObject
import java.io.StringReader
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

object SharedJsScope {

    private val scopeMap = object : LinkedHashMap<String, WeakReference<Scriptable>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, WeakReference<Scriptable>>?): Boolean {
            return size > 16
        }
    }
    private const val CRYPTO_JS_PATH = "/js/cryptojs.min.js"
    @Volatile
    private var cryptoJsText: String? = null
    @Volatile
    private var cryptoScope: WeakReference<Scriptable>? = null
    private val cryptoLock = Any()

    private fun loadCryptoJs(): String? {
        val cached = cryptoJsText
        if (cached != null) return cached
        return try {
            val text = javaClass.getResourceAsStream(CRYPTO_JS_PATH)?.bufferedReader()?.readText() ?: return null
            cryptoJsText = text
            text
        } catch (e: Throwable) {
            Debug.log("加载CryptoJS失败: ${e.message}")
            null
        }
    }

    fun getCryptoScope(coroutineContext: CoroutineContext?): Scriptable? {
        val cached = cryptoScope?.get()
        if (cached != null) return cached
        synchronized(cryptoLock) {
            val second = cryptoScope?.get()
            if (second != null) return second
            val text = loadCryptoJs() ?: return null
            return try {
                val bindings = ScriptBindings()
                bindings["coroutineContext"] = coroutineContext
                val scope = RhinoScriptEngine.getRuntimeScope(bindings)
                RhinoScriptEngine.eval(StringReader(text), scope, coroutineContext)
                cryptoScope = WeakReference(scope)
                scope
            } catch (e: Exception) {
                Debug.log("CryptoJS init error: ${e.message}")
                null
            }
        }
    }

    fun getScope(
        coroutineContext: CoroutineContext?,
        scriptText: String,
        shareScopeName: String?
    ): Scriptable {
        val bindings = ScriptBindings()
        bindings["coroutineContext"] = coroutineContext
        var topScope = RhinoScriptEngine.getRuntimeScope(bindings)

        if (!shareScopeName.isNullOrEmpty()) {
            val weakRef = scopeMap[shareScopeName]
            val shared = weakRef?.get()
            if (shared != null) {
                topScope = shared
            } else {
                scopeMap[shareScopeName] = WeakReference(topScope)
            }
        }

        val cryptoScope = getCryptoScope(coroutineContext)
        if (cryptoScope != null) {
            for (id in cryptoScope.ids) {
                if (id is String) {
                    try {
                        ScriptableObject.putProperty(topScope, id, cryptoScope.get(id, cryptoScope))
                    } catch (_: Exception) {}
                }
            }
        }

        RhinoScriptEngine.eval(StringReader(scriptText), topScope, coroutineContext)
        return topScope
    }

    fun getCacheJs(
        coroutineContext: CoroutineContext?,
        name: String,
        jsText: String?,
        shareScopeName: String?
    ): Scriptable {
        val cacheKey = "${name}_${io.legado.server.utils.MD5Utils.md5(jsText ?: "")}"
        scopeMap[cacheKey]?.get()?.let { return it }
        val scope = getScope(coroutineContext, jsText ?: "", shareScopeName)
        scopeMap[cacheKey] = WeakReference(scope)
        return scope
    }
}

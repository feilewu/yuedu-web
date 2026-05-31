package io.legado.server.rhino

import com.script.rhino.JavaObjectWrapFactory
import org.htmlunit.corejs.javascript.NativeJavaObject
import org.htmlunit.corejs.javascript.VarScope
import org.htmlunit.corejs.javascript.lc.type.TypeInfo
import org.htmlunit.corejs.javascript.lc.type.TypeInfoFactory

class NativeBaseSource(scope: VarScope?, javaObject: Any, staticType: Class<*>?) :
    NativeJavaObject(scope, javaObject, staticType.toTypeInfo()) {

    override fun has(name: String, start: org.htmlunit.corejs.javascript.Scriptable): Boolean {
        if (name != "setVariable" && name.length > 3 && name.startsWith("set")) {
            val fieldName = name.substring(3).replaceFirstChar { it.lowercase() }
            if (super.has(fieldName, start)) {
                return false
            }
        }
        return super.has(name, start)
    }

    override fun get(name: String, start: org.htmlunit.corejs.javascript.Scriptable): Any? {
        if (name != "setVariable" && name.length > 3 && name.startsWith("set")) {
            val fieldName = name.substring(3).replaceFirstChar { it.lowercase() }
            if (super.has(fieldName, start)) {
                return NOT_FOUND
            }
        }
        return super.get(name, start)
    }

    override fun put(name: String, start: org.htmlunit.corejs.javascript.Scriptable, value: Any?) {
        if (name == "variable") {
            super.put(name, start, value)
        }
    }

    companion object {
        val factory = JavaObjectWrapFactory { scope, javaObject, staticType ->
            NativeBaseSource(scope, javaObject, staticType)
        }
    }
}

private fun Class<*>?.toTypeInfo(): TypeInfo {
    return this?.let { TypeInfoFactory.GLOBAL.create(it) } ?: TypeInfo.NONE
}

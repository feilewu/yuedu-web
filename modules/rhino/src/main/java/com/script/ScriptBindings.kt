package com.script

import org.htmlunit.corejs.javascript.Context
import org.htmlunit.corejs.javascript.NativeObject
import org.htmlunit.corejs.javascript.Scriptable
import org.htmlunit.corejs.javascript.TopLevel
import org.htmlunit.corejs.javascript.VarScope

class ScriptBindings : NativeObject() {

    companion object {
        private val topLevelScope: TopLevel by lazy {
            val cx = Context.enter()
            try {
                cx.initStandardObjects()
            } finally {
                Context.exit()
            }
        }
    }

    init {
        prototype = topLevelScope.globalThis
    }

    operator fun set(key: String, value: Any?) {
        Context.enter()
        try {
            put(key, this as Scriptable, Context.javaToJS(value, topLevelScope))
        } finally {
            Context.exit()
        }
    }

    operator fun set(index: Int, value: Any?) {
        Context.enter()
        try {
            put(index, this as Scriptable, Context.javaToJS(value, topLevelScope))
        } finally {
            Context.exit()
        }
    }

    fun put(key: String, value: Any?) {
        set(key, value)
    }

}

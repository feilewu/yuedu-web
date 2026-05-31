package io.legado.server.source

import io.legado.server.model.entity.BaseSource

fun BaseSource.getShareScope(coroutineContext: kotlin.coroutines.CoroutineContext? = null): org.htmlunit.corejs.javascript.Scriptable? = null
fun BaseSource.getSourceType(): Int = 0
fun BaseSource.getBookType(): Int = 0
val BaseSource.exploreKindsJson: String? get() = null

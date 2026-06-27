package com.eliasrvjimenez.myapplication.util

import com.eliasrvjimenez.myapplication.getPlatform

object Logger {
    private val isDebug = getPlatform().isDebug

    fun d(message: String) {
        if (isDebug) {
            println(message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        // We might want to always log errors, or at least in debug
        if (isDebug) {
            println("ERROR: $message")
            throwable?.printStackTrace()
        }
    }
}

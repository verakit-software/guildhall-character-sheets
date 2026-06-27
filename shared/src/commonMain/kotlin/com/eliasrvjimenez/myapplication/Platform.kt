package com.eliasrvjimenez.myapplication

interface Platform {
    val name: String
    val isDebug: Boolean
}

expect fun getPlatform(): Platform
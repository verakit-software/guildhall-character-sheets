package com.eliasrvjimenez.guildhall

import android.os.Build

class AndroidPlatform(
    override val name: String,
    override val isDebug: Boolean
) : Platform

actual fun getPlatform(): Platform = AndroidPlatform(
    name = "Android ${Build.VERSION.SDK_INT}",
    isDebug = true // We can refine this if we pass context, but default to true for now or use a global flag
)
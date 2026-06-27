package com.eliasrvjimenez.myapplication

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.eliasrvjimenez.myapplication.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "MyApplication",
    ) {
        App()
    }
}
package com.eliasrvjimenez.guildhall

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.eliasrvjimenez.guildhall.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "GuildHall",
    ) {
        App()
    }
}
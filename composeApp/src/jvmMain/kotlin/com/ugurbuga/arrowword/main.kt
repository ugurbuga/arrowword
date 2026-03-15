package com.ugurbuga.arrowword

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = { exitApplication() },
        title = "Arrow Word",
    ) {
        App()
    }
}
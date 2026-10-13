package com.onelinediary.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 440.dp, height = 820.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "한줄일기 - 우주 어딘가의 기록",
        state = windowState
    ) {
        App()
    }
}

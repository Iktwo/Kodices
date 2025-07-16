package com.iktwo.kodices.sampleapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(title = "Sample Application", onCloseRequest = ::exitApplication) {
            App()
        }
    }

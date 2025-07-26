package com.iktwo.kodices.sampleapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.iktwo.kodices.sampleapp.theme.AppTheme

fun main() =
    application {
        Window(title = "Sample Application", onCloseRequest = ::exitApplication) {
            AppTheme(darkTheme = false) {
                App()
            }
        }
    }

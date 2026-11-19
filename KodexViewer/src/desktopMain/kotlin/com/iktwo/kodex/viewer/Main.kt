package com.iktwo.kodex.viewer

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val config = when (val parsed = parseViewerArgs(args.toList(), System::getenv)) {
        is ViewerArgs.Start -> {
            parsed.config
        }

        ViewerArgs.Help -> {
            println(USAGE)
            return
        }

        is ViewerArgs.Invalid -> {
            System.err.println(parsed.message)
            System.err.println(USAGE)
            exitProcess(2)
        }
    }

    application {
        Window(
            title = "Kodex Viewer",
            state = rememberWindowState(size = DpSize(1280.dp, 860.dp)),
            onCloseRequest = ::exitApplication,
        ) {
            ViewerApp(config)
        }
    }
}

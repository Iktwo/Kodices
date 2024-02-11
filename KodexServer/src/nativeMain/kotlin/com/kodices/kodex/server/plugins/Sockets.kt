package com.kodices.kodex.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriodMillis = 15000
        timeoutMillis = 15000
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/kodices") {
            send(Frame.Text("{}"))
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    // TODO: forward messages
                    val text = frame.readText()
                    // outgoing.send(Frame.Text(""))
                }
            }
        }
    }
}

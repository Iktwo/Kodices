package com.kodices.kodex.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

/**
 * Connected clients. Kotlin/Native has no ConcurrentHashMap; the server is single-threaded per
 * engine dispatcher, so a plain set guarded by the socket handler is enough for a sample.
 */
private val sessions = mutableSetOf<DefaultWebSocketServerSession>()

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriodMillis = 15000
        timeoutMillis = 15000
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/kodices") {
            // Broadcast to every connected client, so a UI definition pushed by one
            // connection reaches the others.
            sessions.add(this)

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        sessions.forEach { session ->
                            runCatching { session.send(Frame.Text(text)) }
                        }
                    }
                }
            } finally {
                sessions.remove(this)
            }
        }
    }
}

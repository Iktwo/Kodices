package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

val client = HttpClient {
    install(WebSockets) { }
}

@Composable
fun TabWebsockets() {
    val scope = rememberCoroutineScope()

    scope.launch(Dispatchers.IO) {
        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = "127.0.0.1",
                port = 8080,
                path = "/kodices",
            ) {
                val messageOutputRoutine = launch { outputMessages() }

                messageOutputRoutine.cancelAndJoin()
            }
        } catch (e: Exception) {
            println("e: $e")
        }
    }
}

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            println(message.readText())
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e)
    }
}

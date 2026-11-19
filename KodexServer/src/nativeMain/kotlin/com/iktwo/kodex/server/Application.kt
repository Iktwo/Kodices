package com.iktwo.kodex.server

import com.iktwo.kodex.server.plugins.configureRouting
import com.iktwo.kodex.server.plugins.configureSerialization
import com.iktwo.kodex.server.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

fun main() {
    embeddedServer(CIO, port = 8081, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets()
    configureRouting()
}

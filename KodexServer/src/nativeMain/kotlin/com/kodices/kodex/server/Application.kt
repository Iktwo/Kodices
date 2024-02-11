package com.kodices.kodex.server

import com.kodices.kodex.server.plugins.configureRouting
import com.kodices.kodex.server.plugins.configureSerialization
import com.kodices.kodex.server.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets()
    configureRouting()
}

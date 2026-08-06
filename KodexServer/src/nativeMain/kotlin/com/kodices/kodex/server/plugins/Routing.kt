package com.kodices.kodex.server.plugins

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * A sample JSON UI definition, standing in for whatever a real server would build per request.
 */
private val SAMPLE_CONTENT = """
    {
      "elements": [
        { "type": "topbar", "text": "KodexServer" },
        { "type": "row", "id": "greeting", "text": "Served by KodexServer", "textSecondary": "GET /content" },
        { "type": "separator" }
      ]
    }
""".trimIndent()

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK")
        }

        get("/content") {
            call.respondText(SAMPLE_CONTENT, ContentType.Application.Json)
        }
    }
}

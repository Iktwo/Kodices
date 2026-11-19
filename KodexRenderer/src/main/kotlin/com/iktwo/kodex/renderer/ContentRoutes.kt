package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.Renderer
import com.iktwo.kodex.renderer.render.ResolvedScreen
import com.iktwo.kodex.renderer.render.ScreenParseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFilePath
import io.ktor.http.encodeURLPath
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.io.path.readBytes

/*
 * The other way to show a screen: instead of a picture, the client gets the screen's JSON and
 * renders it itself with Kodices and Piktographs (an app, as opposed to an e-ink panel).
 *
 *   GET /screens                 which screens exist, and which one is the default
 *   GET /content/{screen}.json   {"content": ..., "data": ..., "refresh_seconds": ...}
 *   GET /assets/{path}           the images screens refer to as asset:<path>
 */

/** Response of `GET /screens`. */
@Serializable
public data class ScreensResponse(
    @SerialName("default") val defaultScreen: String,
    @SerialName("screens") val screens: List<String>,
)

internal fun Route.contentRoutes(
    config: RendererConfig,
    service: RenderService,
) {
    get("/screens") {
        call.respond(ScreensResponse(defaultScreen = config.defaultScreen, screens = service.screens()))
    }

    get("/content/{screen}.json") {
        val screen = call.parameters["screen"].orEmpty()
        val resolved = try {
            service.resolve(screen)
        } catch (e: ScreenParseException) {
            call.respondText(e.message.orEmpty(), status = HttpStatusCode.UnprocessableEntity)
            return@get
        }

        if (resolved == null) {
            call.respondText("Unknown screen '$screen'", status = HttpStatusCode.NotFound)
            return@get
        }

        val body = contentEnvelope(resolved, config.refreshSeconds, config.publicUrl ?: call.requestBaseUrl())
            .toString()
            .toByteArray()
        val etag = Renderer.strongEtag(body)

        call.response.header(HttpHeaders.ETag, etag)
        call.response.header(HttpHeaders.CacheControl, "no-cache")

        if (matchesIfNoneMatch(call.request.headers[HttpHeaders.IfNoneMatch], etag)) {
            call.respond(HttpStatusCode.NotModified)
        } else {
            call.respondBytes(body, ContentType.Application.Json)
        }
    }

    get("/assets/{path...}") {
        val path = call.parameters.getAll("path").orEmpty().joinToString("/")
        val file = service.asset(path)

        if (file == null) {
            call.respondText("Unknown asset", status = HttpStatusCode.NotFound)
            return@get
        }

        val bytes = file.readBytes()
        val etag = Renderer.strongEtag(bytes)

        call.response.header(HttpHeaders.ETag, etag)
        call.response.header(HttpHeaders.CacheControl, "no-cache")

        if (matchesIfNoneMatch(call.request.headers[HttpHeaders.IfNoneMatch], etag)) {
            call.respond(HttpStatusCode.NotModified)
        } else {
            call.respondBytes(bytes, ContentType.defaultForFilePath(file.fileName.toString()))
        }
    }
}

/**
 * What `/content/{screen}.json` returns: the shape `KodicesParser.parseJSONWithDataToContent`
 * reads, plus how long the client should wait before asking again.
 *
 * `asset:` URLs only mean something on this machine, so they are rewritten to this server's
 * `/assets/` route under [baseUrl].
 */
internal fun contentEnvelope(
    screen: ResolvedScreen,
    refreshSeconds: Int,
    baseUrl: String,
): JsonObject =
    buildJsonObject {
        put("content", screen.content.rewriteAssetUrls(baseUrl))
        put("data", screen.data.rewriteAssetUrls(baseUrl))
        put("refresh_seconds", refreshSeconds)
    }

private fun JsonElement.rewriteAssetUrls(baseUrl: String): JsonElement =
    when (this) {
        is JsonObject -> JsonObject(mapValues { (_, value) -> value.rewriteAssetUrls(baseUrl) })
        is JsonArray -> JsonArray(map { it.rewriteAssetUrls(baseUrl) })
        is JsonPrimitive ->
            if (isString && content.startsWith(ASSET_SCHEME)) {
                JsonPrimitive("$baseUrl/$ASSETS_DIR/${content.removePrefix(ASSET_SCHEME).encodeURLPath()}")
            } else {
                this
            }
    }

/** `scheme://host[:port]` as the client addressed this server. */
private fun ApplicationCall.requestBaseUrl(): String {
    val origin = request.origin
    val host = request.headers[HttpHeaders.Host] ?: "${origin.serverHost}:${origin.serverPort}"
    return "${origin.scheme}://$host"
}

internal const val ASSET_SCHEME = "asset:"

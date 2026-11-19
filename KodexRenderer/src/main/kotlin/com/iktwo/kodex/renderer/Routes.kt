package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.RenderMode
import com.iktwo.kodex.renderer.render.RenderResult
import com.iktwo.kodex.renderer.render.RenderSpec
import com.iktwo.kodex.renderer.render.RenderTimeoutException
import com.iktwo.kodex.renderer.render.ScreenParseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /display`. Field names are the device contract; do not rename them. */
@Serializable
public data class DisplayResponse(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("refresh_seconds") val refreshSeconds: Int,
    @SerialName("etag") val etag: String,
)

internal fun Application.configureRouting(
    config: RendererConfig,
    service: RenderService,
) {
    routing {
        get("/health") {
            call.respondText("ok")
        }

        if (config.token != null) {
            authenticate(AUTH_PROVIDER) {
                deviceRoutes(config, service)
                contentRoutes(config, service)
            }
        } else {
            deviceRoutes(config, service)
            contentRoutes(config, service)
        }
    }
}

private fun Route.deviceRoutes(
    config: RendererConfig,
    service: RenderService,
) {
    get("/display") {
        // Every device (`?device=<id>`) gets the default screen for now; the parameter is part of
        // the contract so per-device screens can be added without changing the device side.
        val spec = config.display
        val screen = config.defaultScreen

        val result = call.renderOrRespondError(service, screen, spec) ?: return@get
        call.respond(
            DisplayResponse(
                imageUrl = "/render/$screen.png?width=${spec.width}&height=${spec.height}&mode=${spec.mode.id}",
                refreshSeconds = config.refreshSeconds,
                etag = result.etag,
            ),
        )
    }

    get("/render/{screen}.png") {
        val screen = call.parameters["screen"].orEmpty()
        val spec = try {
            parseSpec(call.request.queryParameters, config.display)
        } catch (e: IllegalArgumentException) {
            call.respondText(e.message.orEmpty(), status = HttpStatusCode.BadRequest)
            return@get
        }

        val result = call.renderOrRespondError(service, screen, spec) ?: return@get

        call.response.header(HttpHeaders.ETag, result.etag)
        call.response.header(HttpHeaders.CacheControl, "no-cache")
        call.response.header(RENDER_TIME_HEADER, result.renderMillis.toString())
        call.response.header(RENDER_CACHE_HEADER, if (result.cached) "hit" else "miss")

        if (matchesIfNoneMatch(call.request.headers[HttpHeaders.IfNoneMatch], result.etag)) {
            call.respond(HttpStatusCode.NotModified)
        } else {
            call.respondBytes(result.png, ContentType.Image.PNG)
        }
    }

    get("/render/{screen}.json") {
        val source = service.source(call.parameters["screen"].orEmpty())
        if (source == null) {
            call.respondText("Unknown screen", status = HttpStatusCode.NotFound)
        } else {
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.respondText(source, ContentType.Application.Json)
        }
    }
}

/** Renders, or responds with the matching error status and returns null. */
private suspend fun ApplicationCall.renderOrRespondError(
    service: RenderService,
    screen: String,
    spec: RenderSpec,
): RenderResult? {
    return try {
        service.render(screen, spec)
            ?: run {
                respondText("Unknown screen '$screen'", status = HttpStatusCode.NotFound)
                null
            }
    } catch (e: ScreenParseException) {
        respondText(e.message.orEmpty(), status = HttpStatusCode.UnprocessableEntity)
        null
    } catch (e: RenderTimeoutException) {
        response.header(HttpHeaders.RetryAfter, RETRY_AFTER_SECONDS.toString())
        respondText(e.message.orEmpty(), status = HttpStatusCode.ServiceUnavailable)
        null
    }
}

internal fun parseSpec(
    query: Parameters,
    defaults: RenderSpec,
): RenderSpec {
    fun int(name: String): Int? = query[name]?.let { it.toIntOrNull() ?: throw IllegalArgumentException("$name must be an integer") }

    val mode = query["mode"]?.let {
        RenderMode.fromId(it) ?: throw IllegalArgumentException("mode must be one of ${RenderMode.entries.joinToString { m -> m.id }}")
    }
    val density = query["density"]?.let {
        it.toFloatOrNull() ?: throw IllegalArgumentException("density must be a number")
    }

    return RenderSpec(
        width = int("width") ?: defaults.width,
        height = int("height") ?: defaults.height,
        mode = mode ?: defaults.mode,
        density = density ?: defaults.density,
    )
}

/** RFC 9110 weak comparison, as `If-None-Match` requires: `W/` prefixes are ignored. */
internal fun matchesIfNoneMatch(
    header: String?,
    etag: String,
): Boolean {
    if (header == null) return false
    val tags = header.split(',').map { it.trim().removePrefix("W/") }
    return tags.any { it == "*" || it == etag }
}

internal const val AUTH_PROVIDER = "kodex-token"
private const val RENDER_TIME_HEADER = "X-Render-Millis"
private const val RENDER_CACHE_HEADER = "X-Render-Cache"
private const val RETRY_AFTER_SECONDS = 30

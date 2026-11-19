package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.Renderer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import org.slf4j.event.Level
import java.security.MessageDigest

public fun main(args: Array<String>) {
    // Before anything touches AWT: rendering never needs a display server.
    System.setProperty("java.awt.headless", "true")
    EngineMain.main(args)
}

/** Entry point referenced from `application.conf`. */
public fun Application.module() {
    val config = RendererConfig.from(environment.config)
    rendererModule(
        config = config,
        renderer = Renderer(
            zoneId = config.zoneId,
            locale = config.locale,
            assetsDir = config.screensDir.resolve(ASSETS_DIR),
        ),
    )
}

public fun Application.rendererModule(
    config: RendererConfig,
    renderer: Renderer,
) {
    environment.log.info("Serving screens from {}", config.screensDir.toAbsolutePath().normalize())

    install(ContentNegotiation) { json() }
    install(CallLogging) {
        level = Level.INFO
        filter { it.request.path() != "/health" }
    }

    config.token?.let { expected ->
        install(Authentication) {
            bearer(AUTH_PROVIDER) {
                authenticate { credential ->
                    if (constantTimeEquals(credential.token, expected)) DEVICE_PRINCIPAL else null
                }
            }
        }
    }

    configureRouting(
        config = config,
        service = RenderService(ScreenRepository(config.screensDir), renderer),
    )
}

private fun constantTimeEquals(
    a: String,
    b: String,
): Boolean = MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

/**
 * Screens reference bundled images as `asset:<file>`, resolved under `<screensDir>/assets`. The
 * renderer reads them from disk; `/content` rewrites them to `/assets/<file>` URLs for clients.
 */
public const val ASSETS_DIR: String = "assets"

private const val DEVICE_PRINCIPAL = "device"

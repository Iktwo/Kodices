package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.RenderMode
import com.iktwo.kodex.renderer.render.RenderSpec
import io.ktor.server.config.ApplicationConfig
import java.nio.file.Path
import java.time.ZoneId
import java.util.Locale
import kotlin.io.path.Path

/**
 * Server settings, read from the `kodex` block of `application.conf`. Every key there can be
 * overridden by an environment variable, see that file.
 *
 * @property token shared secret; when set, `/display` and `/render` require `Authorization: Bearer <token>`.
 * @property display what `/display` points devices at.
 */
public data class RendererConfig(
    val screensDir: Path = Path(DEFAULT_SCREENS_DIR),
    val defaultScreen: String = DEFAULT_SCREEN,
    val refreshSeconds: Int = DEFAULT_REFRESH_SECONDS,
    val token: String? = null,
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val locale: Locale = Locale.US,
    val display: RenderSpec = RenderSpec(),
) {
    init {
        require(ScreenRepository.isValidName(defaultScreen)) { "Invalid default screen name '$defaultScreen'" }
        require(refreshSeconds > 0) { "refreshSeconds must be positive" }
    }

    public companion object {
        public const val DEFAULT_SCREENS_DIR: String = "KodexRenderer/screens"
        public const val DEFAULT_SCREEN: String = "dashboard"
        public const val DEFAULT_REFRESH_SECONDS: Int = 900

        public fun from(config: ApplicationConfig): RendererConfig {
            val kodex = config.config("kodex")

            fun string(key: String): String? = kodex.propertyOrNull(key)?.getString()?.takeIf { it.isNotBlank() }

            val mode = string("display.mode")?.let {
                RenderMode.fromId(it) ?: error("Unknown display.mode '$it', expected one of ${RenderMode.entries.map { m -> m.id }}")
            } ?: RenderMode.Default

            return RendererConfig(
                screensDir = Path(string("screensDir") ?: DEFAULT_SCREENS_DIR),
                defaultScreen = string("defaultScreen") ?: DEFAULT_SCREEN,
                refreshSeconds = string("refreshSeconds")?.toInt() ?: DEFAULT_REFRESH_SECONDS,
                token = string("token"),
                zoneId = string("timeZone")?.let(ZoneId::of) ?: ZoneId.systemDefault(),
                locale = string("locale")?.let(Locale::forLanguageTag) ?: Locale.US,
                display = RenderSpec(
                    width = string("display.width")?.toInt() ?: RenderSpec.DEFAULT_WIDTH,
                    height = string("display.height")?.toInt() ?: RenderSpec.DEFAULT_HEIGHT,
                    mode = mode,
                    density = string("display.density")?.toFloat() ?: RenderSpec.DEFAULT_DENSITY,
                ),
            )
        }
    }
}

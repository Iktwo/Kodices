package com.iktwo.kodex.renderer.render

/** How the rendered page is turned into the PNG the device receives. */
public enum class RenderMode(
    public val id: String,
) {
    /** Grayscale quantized to 16 levels, written as an 8-bit grayscale PNG. */
    Gray16("gray16"),

    /** Floyd–Steinberg dithered to pure black and white, written as an 8-bit grayscale PNG. */
    BlackWhite("bw"),

    /** Unprocessed RGB, for experiments with the colour layer. */
    Color("color"),
    ;

    public companion object {
        public val Default: RenderMode = Gray16

        public fun fromId(id: String): RenderMode? = entries.firstOrNull { it.id == id }
    }
}

/**
 * What to render a screen at.
 *
 * @property width output width in pixels.
 * @property height output height in pixels.
 * @property density pixels per dp. 2.0 keeps 1 dp lines at 2 px, the thinnest the panel shows cleanly.
 */
public data class RenderSpec(
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val mode: RenderMode = RenderMode.Default,
    val density: Float = DEFAULT_DENSITY,
) {
    init {
        require(width in SIZE_RANGE) { "width must be in $SIZE_RANGE, was $width" }
        require(height in SIZE_RANGE) { "height must be in $SIZE_RANGE, was $height" }
        require(density in DENSITY_MIN..DENSITY_MAX) { "density must be in $DENSITY_MIN..$DENSITY_MAX, was $density" }
    }

    public companion object {
        /** reMarkable Paper Pro Move, portrait. */
        public const val DEFAULT_WIDTH: Int = 954
        public const val DEFAULT_HEIGHT: Int = 1696
        public const val DEFAULT_DENSITY: Float = 2f

        public val SIZE_RANGE: IntRange = 64..2000
        public const val DENSITY_MIN: Float = 0.5f
        public const val DENSITY_MAX: Float = 4f
    }
}

/**
 * A rendered screen. [etag] is a strong validator: equal tags mean byte-identical [png].
 *
 * @property renderMillis how long the render took when it ran, also for a [cached] result.
 * @property cached whether this came from the render cache instead of a fresh render.
 */
public class RenderResult(
    public val png: ByteArray,
    public val etag: String,
    public val renderMillis: Long,
    public val cached: Boolean = false,
)

/** The screen did not finish loading its content (images) within the render timeout. */
public class RenderTimeoutException(
    message: String,
) : RuntimeException(message)

/** The screen JSON could not be parsed into Kodices content. */
public class ScreenParseException(
    message: String,
) : RuntimeException(message)

package com.iktwo.kodex.renderer

import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

/**
 * Screens are `<name>.json` files in [screensDir]. They are read on every request, so editing a
 * file shows up on the next render without a restart.
 */
public class ScreenRepository(
    public val screensDir: Path,
) {
    /** The screen's JSON, or null if [name] is not a valid screen name or no such file exists. */
    public fun read(name: String): String? {
        if (!isValidName(name)) return null
        val file = screensDir.resolve("$name.json")
        return if (file.isRegularFile()) file.readText() else null
    }

    public companion object {
        // Letters, digits, '-' and '_' only: no dots or separators, so a name can never leave screensDir.
        private val NAME = Regex("^[A-Za-z0-9_-]{1,64}$")

        public fun isValidName(name: String): Boolean = NAME.matches(name)
    }
}

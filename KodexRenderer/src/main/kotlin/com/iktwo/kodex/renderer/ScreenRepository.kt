package com.iktwo.kodex.renderer

import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
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

    /** Names of every screen in [screensDir], sorted. */
    public fun list(): List<String> {
        if (!screensDir.isDirectory()) return emptyList()
        return screensDir
            .listDirectoryEntries("*.json")
            .filter { it.isRegularFile() }
            .map { it.nameWithoutExtension }
            .filter(::isValidName)
            .sorted()
    }

    /**
     * The file behind `asset:<path>`, or null if there is none. [path] is resolved under
     * `<screensDir>/assets` and can never leave it.
     */
    public fun asset(path: String): Path? {
        if (path.isBlank()) return null
        val root = screensDir.resolve(ASSETS_DIR).absolute().normalize()
        val file = try {
            root.resolve(path).normalize()
        } catch (e: InvalidPathException) {
            return null
        }
        return file.takeIf { it.startsWith(root) && it != root && it.isRegularFile() }
    }

    public companion object {
        // Letters, digits, '-' and '_' only: no dots or separators, so a name can never leave screensDir.
        private val NAME = Regex("^[A-Za-z0-9_-]{1,64}$")

        public fun isValidName(name: String): Boolean = NAME.matches(name)
    }
}

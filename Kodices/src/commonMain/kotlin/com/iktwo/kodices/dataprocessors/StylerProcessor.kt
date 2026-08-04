// DataProcessor.process(data) has no Json parameter, so these cannot reach the parser-scoped
// logger and fall back to the global one.
@file:Suppress("DEPRECATION")

package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.utils.asString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

public enum class StylerStyle {
    UPPERCASE,
    LOWERCASE,
    PRETTY,
    UNKNOWN,
    ;

    public companion object {
        public fun fromString(name: String): StylerStyle {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

/**
 * [DataProcessor] to style values.
 *
 * This can be used to transform strings into UPPERCASE, lowercase, or pretty print JSON.
 */
@Serializable
public data class StylerProcessor(
    val element: String,
) : DataProcessor {
    override val type: String = TYPE

    override fun process(data: JsonElement?): JsonElement? {
        if (data == null) {
            return null
        }

        return when (StylerStyle.fromString(element)) {
            StylerStyle.UPPERCASE -> {
                JsonPrimitive(data.asString().uppercase())
            }

            StylerStyle.LOWERCASE -> {
                JsonPrimitive(data.asString().lowercase())
            }

            StylerStyle.PRETTY -> {
                JsonPrimitive(json.encodeToString(JsonElement.serializer(), data))
            }

            StylerStyle.UNKNOWN -> {
                KodicesParser.logger.warn("Unknown Style in ${StylerProcessor::class.simpleName}: $element")
                // When Style is unknown the data returned without modifications
                data
            }
        }
    }

    public companion object {
        public const val TYPE: String = "styler"

        private val json =
            Json(builderAction = {
                prettyPrint = true
            })
    }
}

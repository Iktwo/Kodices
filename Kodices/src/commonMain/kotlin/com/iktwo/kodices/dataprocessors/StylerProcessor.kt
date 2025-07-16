package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.Kodices
import com.iktwo.kodices.utils.asString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

enum class StylerStyle {
    UPPERCASE,
    LOWERCASE,
    PRETTY,
    UNKNOWN,
    ;

    companion object {
        fun fromString(name: String): StylerStyle {
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
data class StylerProcessor(
    val element: String,
) : DataProcessor {
    override val type = TYPE

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
                Kodices.logger.warn("Unknown Style in ${StylerProcessor::class.simpleName}: $element")
                // When Style is unknown the data returned without modifications
                data
            }
        }
    }

    companion object {
        const val TYPE = "styler"

        private val json =
            Json(builderAction = {
                prettyPrint = true
            })
    }
}

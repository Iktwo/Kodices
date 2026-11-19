package com.iktwo.kodices.dataprocessors

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * [DataProcessor] that replaces a value with the one listed for it.
 *
 * This is how a screen turns what the data says into what an element needs, without the server
 * having to know about the UI: a count into a tone, a level into an icon name, a code into a label.
 *
 * ```json
 * { "type": "map", "values": { "0": "neutral" }, "default": "warning" }
 * ```
 *
 * The incoming value is looked up in [values] by its text, so the number `0`, the string `"0"` and
 * the boolean `true` (as `"true"`) can all be keys. A value that is not listed becomes [default];
 * without a default it passes through unchanged. Objects and arrays are never listed, and missing
 * data (`null`) is looked up as `"null"`.
 */
@Serializable
public data class MapProcessor(
    val values: Map<String, JsonElement> = emptyMap(),
    @SerialName("default") val defaultValue: JsonElement? = null,
) : DataProcessor {
    override val type: String = TYPE

    override fun process(data: JsonElement?): JsonElement? {
        val key = when (data) {
            null, is JsonNull -> NULL_KEY
            is JsonPrimitive -> data.content
            else -> null
        }

        val mapped = key?.let { values[it] } ?: defaultValue ?: data

        return if (mapped is JsonNull) null else mapped
    }

    public companion object {
        public const val TYPE: String = "map"

        private const val NULL_KEY = "null"
    }
}

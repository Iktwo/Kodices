package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.asString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * [DataProcessor] for injecting data into a string.
 *
 * Usually this is used to show variables in the UI.
 *
 * The token for replacement is the percent symbol '%'.
 *
 * You can provide number replacements, like %0, %1, etc.
 */
@Serializable
public data class StringProcessor(
    val element: String,
) : DataProcessor {
    override val type: String = TYPE

    override fun process(data: JsonElement?): JsonElement {
        return when (data) {
            is JsonArray -> {
                // Substituting in ascending index order would rewrite %1 inside %10, so match
                // every token in a single pass instead. Tokens with no matching entry are left as-is.
                val string = INDEXED_TOKEN.replace(element) { match ->
                    val index = match.groupValues[1].toIntOrNull()

                    data
                        .getOrNull(index ?: -1)
                        ?.asString()
                        ?: match.value
                }

                JsonPrimitive(string)
            }

            else -> {
                JsonPrimitive(element.replace("%", (data ?: JsonNull).asString()))
            }
        }
    }

    public companion object {
        public const val TYPE: String = "string"

        private val INDEXED_TOKEN = Regex("%(\\d+)")
    }
}

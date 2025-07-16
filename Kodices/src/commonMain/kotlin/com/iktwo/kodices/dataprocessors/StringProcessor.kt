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
data class StringProcessor(
    val element: String,
) : DataProcessor {
    override val type = TYPE

    override fun process(data: JsonElement?): JsonElement {
        return when (data) {
            is JsonArray -> {
                var string = element

                data.forEachIndexed { index, jsonElement ->
                    string = string.replace("%$index", jsonElement.asString())
                }

                JsonPrimitive(string)
            }

            else -> {
                JsonPrimitive(element.replace("%", (data ?: JsonNull).asString()))
            }
        }
    }

    companion object {
        const val TYPE = "string"
    }
}

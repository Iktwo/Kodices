package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.asString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class StringProcessor(val element: String) : DataProcessor {
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

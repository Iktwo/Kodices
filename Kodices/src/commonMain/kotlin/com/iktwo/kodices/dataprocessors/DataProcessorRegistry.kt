package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * typealias that defines a function that creates a DataProcessor from a [JsonObject]
 */
typealias DataProcessorBuilder = (Json, JsonObject) -> DataProcessor

object DataProcessorRegistry {
    fun fromJsonObject(jsonObject: JsonObject): DataProcessorBuilder? {
        val type = jsonObject[Constants.TYPE]?.asStringOrNull() ?: ""
        return processors[type]
    }

    val processors: MutableMap<String, DataProcessorBuilder> = mutableMapOf(
        JSONDrillerProcessor.TYPE to { json, jsonObject ->
            json.decodeFromJsonElement(JSONDrillerProcessor.Companion, jsonObject)
        },
        StringProcessor.TYPE to { json, jsonObject ->
            json.decodeFromJsonElement(StringProcessor.serializer(), jsonObject)
        },
        StylerProcessor.TYPE to { json, jsonObject ->
            json.decodeFromJsonElement(StylerProcessor.serializer(), jsonObject)
        },
    )
}

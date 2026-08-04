package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.KodicesRegistry
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * typealias that defines a function that creates a DataProcessor from a [JsonObject]
 */
public typealias DataProcessorBuilder = (Json, JsonObject) -> DataProcessor

/**
 * A process-global registry for [DataProcessorBuilder] instances.
 */
@Deprecated(
    "Global mutable state shared by every parser. Pass processors to KodicesRegistry.of and give " +
        "that registry to KodicesParser instead; this is removed in 1.0.",
)
public object DataProcessorRegistry {
    public fun fromJsonObject(jsonObject: JsonObject): DataProcessorBuilder? {
        val type = jsonObject[Constants.TYPE]?.asStringOrNull() ?: ""
        return processors[type]
    }

    public val processors: MutableMap<String, DataProcessorBuilder> =
        KodicesRegistry.DEFAULT_PROCESSORS.toMutableMap()
}

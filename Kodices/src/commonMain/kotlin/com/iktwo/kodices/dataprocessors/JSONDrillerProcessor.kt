package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.Kodices
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * [DataProcessor] used to retrieve data from a JSON object.
 *
 * This expects a list of [JSONRoute], which can be a String or a Number.
 *
 * String represent JSON keys.
 *
 * Numbers represent indexes of values in JSON arrays.
 */
data class JSONDrillerProcessor(val elements: List<JSONRoute>) : DataProcessor {
    constructor(element: JSONRoute) : this(listOf(element))

    override val type = TYPE

    override fun process(data: JsonElement?): JsonElement? {
        // When no elements are provided use the data at this level
        if (elements.isEmpty()) {
            return data
        }

        var tempValue: JsonElement? = data

        elements.map { element ->
            when (element) {
                is JSONRoute.StringRoute -> {
                    if (tempValue is JsonObject) {
                        tempValue?.jsonObject?.get(element.value)?.let {
                            tempValue = it
                        } ?: run {
                            Kodices.logger.warn(
                                "Missing key ${element.value} at $tempValue in ${JSONDrillerProcessor::class.simpleName}, object was expected",
                            )
                            return null
                        }
                    } else {
                        Kodices.logger.warn(
                            "Unable to process element $element in ${JSONDrillerProcessor::class.simpleName}, object was expected",
                        )
                        return null
                    }
                }

                is JSONRoute.NumberRoute -> {
                    if (tempValue is JsonArray) {
                        if (element.value < (tempValue?.jsonArray?.size ?: 0)) {
                            tempValue = tempValue?.jsonArray?.get(element.value)
                        } else {
                            Kodices.logger.warn("Invalid index ${element.value} in ${JSONDrillerProcessor::class.simpleName}")
                            return null
                        }
                    } else {
                        Kodices.logger.warn(
                            "Unable to process element $element in ${JSONDrillerProcessor::class.simpleName}, ${JsonArray::class.simpleName} was expected",
                        )
                        return null
                    }
                }
            }
        }

        return if (tempValue is JsonNull) {
            null
        } else {
            tempValue
        }
    }

    companion object : KSerializer<JSONDrillerProcessor> {
        const val TYPE = "path"

        override val descriptor = JsonArray.serializer().descriptor

        override fun deserialize(decoder: Decoder): JSONDrillerProcessor {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${JSONDrillerProcessor::class.simpleName}"
            }

            val element = decoder.decodeJsonElement()

            check(element is JsonObject && element[Constants.TYPE]?.asStringOrNull() == TYPE) {
                "${JSONDrillerProcessor::class.simpleName} deserialization failed, element must be a ${JsonObject::class.simpleName} of type $TYPE"
            }

            // "element" is ignored if "elements" was also provided
            val pathElements = element[Constants.ELEMENTS] ?: element[Constants.ELEMENT]

            return when {
                pathElements is JsonArray -> {
                    JSONDrillerProcessor(
                        if (pathElements.isEmpty()) {
                            listOf()
                        } else {
                            decoder.json.decodeFromJsonElement(
                                ListSerializer(JSONRoute),
                                pathElements,
                            )
                        },
                    )
                }

                pathElements is JsonPrimitive && (pathElements.isString || pathElements.doubleOrNull != null) -> {
                    JSONDrillerProcessor(decoder.json.decodeFromJsonElement(JSONRoute, pathElements))
                }

                pathElements == null -> {
                    JSONDrillerProcessor(emptyList())
                }

                else -> {
                    throw SerializationException("Failed to deserialize $pathElements as ${JSONDrillerProcessor::class.simpleName}")
                }
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: JSONDrillerProcessor,
        ) {
            throw SerializationException("${JSONDrillerProcessor::class.simpleName} is not serializable")
        }
    }
}

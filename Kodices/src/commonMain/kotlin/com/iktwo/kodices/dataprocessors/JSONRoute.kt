package com.iktwo.kodices.dataprocessors

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull

sealed class JSONRoute {
    /**
     * Class that represents a key in a JSON object
     */
    class StringRoute(val value: String) : JSONRoute()

    /**
     * Class that represents an index in a JSON array
     */
    class NumberRoute(val value: Int) : JSONRoute()

    companion object : KSerializer<JSONRoute> {
        override val descriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): JSONRoute {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${JSONRoute::class.simpleName}"
            }

            val element = decoder.decodeJsonElement()

            return when {
                element is JsonPrimitive && (element.doubleOrNull != null) -> {
                    NumberRoute(element.double.toInt())
                }

                element is JsonPrimitive && element.isString -> {
                    StringRoute(element.content)
                }

                else -> {
                    throw SerializationException("Failed to deserialize $element as ${JSONRoute::class.simpleName}")
                }
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: JSONRoute,
        ) {
            throw SerializationException("${JSONRoute::class.simpleName} is not serializable")
        }
    }
}

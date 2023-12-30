package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class DataProcessorException(message: String) : Exception(message)

/**
 * Interface that defines a method to process data.
 */
@Serializable(with = DataProcessor.Companion::class)
interface DataProcessor {
    /**
     * [String] that represents the type of [DataProcessor]
     */
    val type: String

    /**
     * Function to process data.
     * @param data nullable [JsonElement] to be processed.
     */
    fun process(data: JsonElement?): JsonElement?

    companion object : KSerializer<DataProcessor> {
        override val descriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): DataProcessor {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${DataProcessor::class.simpleName}"
            }

            val jsonObject = decoder.decodeJsonElement().asJSONObjectOrNull()

            check(jsonObject != null) {
                "Failed to deserialize ${DataProcessor::class.simpleName}, object expected"
            }

            DataProcessorRegistry.processors.getOrElse(jsonObject[Constants.TYPE]?.asStringOrNull() ?: "") {
                throw SerializationException("Unable to deserialize ${DataProcessor::class.simpleName}")
            }.let { builder ->
                return builder(decoder.json, jsonObject)
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: DataProcessor,
        ) {
            throw SerializationException("${DataProcessor::class.simpleName} is not serializable")
        }
    }
}

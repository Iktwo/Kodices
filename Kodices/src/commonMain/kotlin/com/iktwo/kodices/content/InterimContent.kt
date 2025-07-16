package com.iktwo.kodices.content

import com.iktwo.kodices.elements.Element
import com.iktwo.kodices.elements.InterimElement
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Class that represents content that has to be processed.
 */
@Serializable(with = InterimContent.Companion::class)
data class InterimContent(
    val elements: List<Element>,
) {
    constructor(element: Element) : this(listOf(element))
    constructor() : this(emptyList())

    /**
     * Function that process the interim content into.
     *
     * @param data optional [JsonElement] that represents data to process elements.
     */
    fun process(
        data: JsonElement?,
        json: Json,
    ): Content {
        return Content(
            elements
                .mapIndexed { index, element ->
                    when (element) {
                        is ProcessedElement -> {
                            listOf(element)
                        }

                        is InterimElement -> {
                            element.process(index = index, data = data, json = json).toList()
                        }
                    }
                }.flatten(),
        )
    }

    companion object : KSerializer<InterimContent> {
        override val descriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): InterimContent {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${InterimContent::class.simpleName}"
            }

            decoder.decodeJsonElement().asJSONObjectOrNull()?.let { jsonObject ->
                val elements: List<Element> = when (val elementsValue = jsonObject[Constants.ELEMENTS] ?: jsonObject[Constants.ELEMENT]) {
                    is JsonArray -> {
                        decoder.json.decodeFromJsonElement(ListSerializer(Element.Companion), elementsValue)
                    }

                    is JsonObject -> {
                        listOf(decoder.json.decodeFromJsonElement(Element.Companion, elementsValue))
                    }

                    else -> {
                        emptyList()
                    }
                }

                return InterimContent(elements)
            } ?: run {
                throw SerializationException("Failed to deserialize ${InterimContent::class.simpleName}, object expected")
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: InterimContent,
        ) {
            throw SerializationException("${InterimContent::class.simpleName} is not serializable")
        }
    }
}

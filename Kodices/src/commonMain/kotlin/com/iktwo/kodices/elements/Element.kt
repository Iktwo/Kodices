package com.iktwo.kodices.elements

import com.iktwo.kodices.Kodices
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.InterimAction
import com.iktwo.kodices.dataprocessors.DataProcessor
import com.iktwo.kodices.dataprocessors.DataProcessorException
import com.iktwo.kodices.dataprocessors.DataProcessorRegistry
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONArrayOrNull
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asMap
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Sealed interface that defines the concept of an Element.
 *
 * An Element is meant to be mapped to an individual UI element. Elements may have nested elements in them.
 */
@Serializable(with = Element.Companion::class)
sealed interface Element {
    /**
     * Represents the [type] of this element.
     */
    val type: String

    /**
     * [List] of [Element] that are contained in this [Element].
     */
    val nestedElements: List<Element>

    /**
     * [Boolean] that defines if this [Element] is enabled.
     * This does not enforce any behavior, elements may use this in different ways.
     *
     * Defaults to true.
     */
    val enabled: Boolean
        get() = true

    /**
     * [List] of [Action] for this [Element].
     */
    val actions: List<Action>

    companion object : KSerializer<Element> {
        override val descriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): Element {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${Element::class.simpleName}"
            }

            val jsonObject = decoder.decodeJsonElement()

            check(jsonObject is JsonObject) {
                "Failed to deserialize ${Element::class.simpleName}, ${JsonObject::class.simpleName} expected"
            }

            val type = jsonObject[Constants.TYPE]?.asStringOrNull()

            checkNotNull(type) {
                "Unable to create ${Element::class.simpleName} without type"
            }

            val actions = getActions(decoder.json, jsonObject)

            val processors = jsonObject[Constants.PROCESSORS]?.asJSONObjectOrNull()
            val constants = jsonObject[Constants.CONSTANTS]?.asJSONObjectOrNull()
            val expandWithProcessors = jsonObject[Constants.EXPAND_WITH_PROCESSOR] ?: jsonObject[Constants.EXPAND_WITH_PROCESSORS]
            val id = jsonObject[Constants.ID]?.asStringOrNull()
            val nestedElements = jsonObject[Constants.NESTED_ELEMENTS]?.asJSONArrayOrNull()

            // If there are processors, constants or processors for expansion, treat this as an InterimElement
            if (processors != null || constants != null || expandWithProcessors != null) {
                val processorsForExpansion = mutableListOf<DataProcessor>()

                expandWithProcessors?.let {
                    when (it) {
                        is JsonObject -> {
                            processorsForExpansion.add(
                                decoder.json.decodeFromJsonElement<DataProcessor>(it),
                            )
                        }

                        is JsonArray -> {
                            processorsForExpansion.addAll(
                                decoder.json.decodeFromJsonElement<List<DataProcessor>>(it),
                            )
                        }

                        else -> {
                            throw SerializationException(
                                "expandWithProcessor(s) has to be a ${JsonArray::class.simpleName} or ${JsonObject::class.simpleName}",
                            )
                        }
                    }
                }

                val dataProcessors = processors?.map { (property, jsonElement) ->
                    when (jsonElement) {
                        is JsonArray -> {
                            return@map property to jsonElement.jsonArray.map {
                                DataProcessorRegistry.fromJsonObject(it.jsonObject)?.let { dataProcessorBuilder ->
                                    dataProcessorBuilder(decoder.json, it.jsonObject)
                                } ?: run {
                                    throw DataProcessorException("invalid ${jsonElement.jsonObject} in ${DataProcessor::class.simpleName} not supported")
                                }
                            }
                        }

                        is JsonObject -> {
                            DataProcessorRegistry.fromJsonObject(jsonElement.jsonObject)?.let { dataProcessorBuilder ->
                                return@map property to
                                    listOf(
                                        dataProcessorBuilder(
                                            decoder.json,
                                            jsonElement.jsonObject,
                                        ),
                                    )
                            } ?: run {
                                throw DataProcessorException("$property ${DataProcessor::class.simpleName} not registered")
                            }
                        }

                        else -> {
                            throw DataProcessorException("invalid ${jsonElement.jsonObject} in ${DataProcessor::class.simpleName} not supported")
                        }
                    }
                }?.toMap() ?: emptyMap()

                return InterimElement(
                    type = type,
                    nestedElements = nestedElements?.map { decoder.json.decodeFromJsonElement<Element>(it) } ?: emptyList(),
                    id = id,
                    constants = constants,
                    processors = dataProcessors,
                    processorsForExpansion = processorsForExpansion,
                    actions = actions,
                )
            } else {
                return resolveProcessedElement(jsonObject)
            }
        }

        /**
         * Function that resolves a [ProcessedElement].
         *
         * This checks the [ElementRegistry], if there is a builder for the type, it calls it.
         * If the type is not in the registry then this creates a [ProcessedElement].
         *
         * Note: Creating a [ProcessedElement] this way does not support [Action]
         */
        private fun resolveProcessedElement(jsonObject: JsonObject): ProcessedElement {
            val type = jsonObject[Constants.TYPE]?.asStringOrNull()

            checkNotNull(type) {
                "Unable to create ${Element::class.simpleName} without type"
            }

            val id = jsonObject[Constants.ID]?.asStringOrNull() ?: "id"

            val nestedElements = jsonObject[Constants.NESTED_ELEMENTS]?.asJSONArrayOrNull()?.mapNotNull {
                if (it is JsonObject) resolveProcessedElement(it) else null
            } ?: emptyList()
            val text = jsonObject[Constants.TEXT_KEY]?.asStringOrNull()
            val textSecondary = jsonObject[Constants.TEXT_SECONDARY_KEY]?.asStringOrNull()

            return ElementRegistry.getElement(jsonObject[Constants.TYPE]?.asStringOrNull() ?: "")?.let { builder ->
                builder(
                    id,
                    jsonObject.asMap().toMutableMap(),
                    nestedElements,
                    emptyList(),
                )
            } ?: ProcessedElement(
                type = type,
                id = id,
                nestedElements = nestedElements,
                text = text,
                textSecondary = textSecondary,
                actions = emptyList(),
                jsonValues = jsonObject.asMap().toMutableMap(),
            )
        }

        override fun serialize(
            encoder: Encoder,
            value: Element,
        ) {
            throw SerializationException("${Element::class.simpleName} is not serializable")
        }

        /**
         * Function that extracts the actions in a JsonObject.
         *
         * Providing either a single action or an array of actions is possible.
         */
        private fun getActions(
            json: Json,
            jsonObject: JsonObject,
        ): List<InterimAction> {
            if (jsonObject.containsKey(Constants.ACTIONS) && jsonObject.containsKey(Constants.ACTION)) {
                Kodices.logger.warn("An element provided both ${Constants.ACTION} and ${Constants.ACTIONS}. That is usually a mistake. ${Constants.ACTIONS} will be used.")
            }

            val actionsValue = jsonObject[Constants.ACTIONS] ?: jsonObject[Constants.ACTION]

            return when (actionsValue) {
                is JsonArray -> {
                    json.decodeFromJsonElement(
                        ListSerializer(InterimAction.serializer()),
                        actionsValue,
                    )
                }

                is JsonObject -> {
                    listOf(
                        json.decodeFromJsonElement(
                            InterimAction.serializer(),
                            actionsValue,
                        ),
                    )
                }

                else -> {
                    if (actionsValue != null) {
                        Kodices.logger.warn(
                            "Invalid type found for ${Action::class.simpleName}. It must be either an object or an array, provided value: $actionsValue",
                        )
                    }
                    emptyList()
                }
            }
        }
    }
}

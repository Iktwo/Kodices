package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.InterimAction
import com.iktwo.kodices.dataprocessors.DataProcessor
import com.iktwo.kodices.dataprocessors.DataProcessorException
import com.iktwo.kodices.kodicesContext
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONArrayOrNull
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asMap
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
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
public sealed interface Element {
    /**
     * Represents the [type] of this element.
     */
    public val type: String

    /**
     * [List] of [Element] that are contained in this [Element].
     */
    public val nestedElements: List<Element>

    /**
     * [Boolean] that defines if this [Element] is enabled.
     * This does not enforce any behavior, elements may use this in different ways.
     *
     * Defaults to true.
     */
    public val enabled: Boolean
        get() = true

    /**
     * [Boolean] that defines if this [Element] is visible.
     * This does not enforce any behavior, elements may use this in different ways.
     *
     * Defaults to true.
     */
    public val visible: Boolean
        get() = true

    /**
     * [List] of [Action] for this [Element].
     */
    public val actions: List<Action>

    public companion object : KSerializer<Element> {
        override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor = JsonObject.serializer().descriptor

        override fun deserialize(decoder: Decoder): Element {
            check(decoder is JsonDecoder) {
                "Only ${JsonDecoder::class.simpleName} is supported for ${Element::class.simpleName}"
            }

            val jsonObject = decoder.decodeJsonElement()

            check(jsonObject is JsonObject) {
                "Failed to deserialize ${Element::class.simpleName}, ${JsonObject::class.simpleName} expected"
            }

            return deserialize(decoder.json, jsonObject)
        }

        /**
         * Deserializes a single [Element] with knowledge of its position among its siblings.
         *
         * The [KSerializer] entry point above cannot supply a position, so callers that decode a
         * list of elements go through here instead: [index] and [parentId] are what make a
         * generated id unique when the JSON does not provide one.
         */
        internal fun deserialize(
            json: Json,
            jsonObject: JsonObject,
            index: Int = 0,
            parentId: String? = null,
        ): Element {
            val type = jsonObject[Constants.TYPE]?.asStringOrNull()

            checkNotNull(type) {
                "Unable to create ${Element::class.simpleName} without type"
            }

            val actions = getActions(json, jsonObject)

            val processors = jsonObject[Constants.PROCESSORS]?.asJSONObjectOrNull()
            val constants = jsonObject[Constants.CONSTANTS]?.asJSONObjectOrNull()
            val expandWithProcessors = jsonObject[Constants.EXPAND_WITH_PROCESSOR]
                ?: jsonObject[Constants.EXPAND_WITH_PROCESSORS]
            val id = jsonObject[Constants.ID]?.asStringOrNull()
            val nestedElements = jsonObject[Constants.NESTED_ELEMENTS]?.asJSONArrayOrNull()

            // If there are processors, constants or processors for expansion, treat this as an InterimElement
            if (processors != null || constants != null || expandWithProcessors != null) {
                val processorsForExpansion = mutableListOf<DataProcessor>()

                expandWithProcessors?.let {
                    when (it) {
                        is JsonObject -> {
                            processorsForExpansion.add(
                                json.decodeFromJsonElement<DataProcessor>(it),
                            )
                        }

                        is JsonArray -> {
                            processorsForExpansion.addAll(
                                json.decodeFromJsonElement<List<DataProcessor>>(it),
                            )
                        }

                        else -> {
                            throw SerializationException(
                                "expandWithProcessor(s) has to be a ${JsonArray::class.simpleName} or ${JsonObject::class.simpleName}",
                            )
                        }
                    }
                }

                val dataProcessors = processors
                    ?.map { (property, jsonElement) ->
                        when (jsonElement) {
                            is JsonArray -> {
                                return@map property to jsonElement.jsonArray.map { entry ->
                                    val entryObject = entry.asJSONObjectOrNull()
                                        ?: throw DataProcessorException(
                                            "invalid $entry in $property, every ${DataProcessor::class.simpleName} has to be an object",
                                        )

                                    json.kodicesContext.registry
                                        .dataProcessorBuilder(entryObject[Constants.TYPE]?.asStringOrNull() ?: "")
                                        ?.let { dataProcessorBuilder ->
                                            dataProcessorBuilder(json, entryObject)
                                        } ?: run {
                                        throw DataProcessorException("invalid $entry in $property, ${DataProcessor::class.simpleName} not registered")
                                    }
                                }
                            }

                            is JsonObject -> {
                                json.kodicesContext.registry
                                    .dataProcessorBuilder(jsonElement.jsonObject[Constants.TYPE]?.asStringOrNull() ?: "")
                                    ?.let { dataProcessorBuilder ->
                                        return@map property to
                                            listOf(
                                                dataProcessorBuilder(
                                                    json,
                                                    jsonElement.jsonObject,
                                                ),
                                            )
                                    } ?: run {
                                    throw DataProcessorException("$property ${DataProcessor::class.simpleName} not registered")
                                }
                            }

                            else -> {
                                throw DataProcessorException(
                                    "invalid $jsonElement in $property, a ${DataProcessor::class.simpleName} has to be an object or an array of objects",
                                )
                            }
                        }
                    }?.toMap() ?: emptyMap()

                return InterimElement(
                    type = type,
                    nestedElements = nestedElements?.mapIndexed { nestedIndex, nested ->
                        deserialize(
                            json,
                            nested.asJSONObjectOrNull() ?: throw SerializationException(
                                "Failed to deserialize a nested ${Element::class.simpleName}, ${JsonObject::class.simpleName} expected",
                            ),
                            nestedIndex,
                        )
                    } ?: emptyList(),
                    id = id,
                    constants = constants,
                    processors = dataProcessors,
                    processorsForExpansion = processorsForExpansion,
                    actions = actions,
                )
            } else {
                return resolveProcessedElement(jsonObject, json, index, parentId)
            }
        }

        /**
         * Function that resolves a [ProcessedElement].
         *
         * This checks the [ElementRegistry], if there is a builder for the type, it calls it.
         * If the type is not in the registry then this creates a [ProcessedElement].
         *
         * When the JSON does not provide an [Constants.ID], one is generated from the [index] and
         * the parent's id, matching the scheme used by [InterimElement]. Ids have to be unique
         * within a [com.iktwo.kodices.content.Content] because renderers use them as list keys.
         *
         * Actions are resolved with no data, since an element on this path has no processors to
         * feed one.
         */
        private fun resolveProcessedElement(
            jsonObject: JsonObject,
            json: Json,
            index: Int = 0,
            parentId: String? = null,
        ): ProcessedElement {
            val type = jsonObject[Constants.TYPE]?.asStringOrNull()

            checkNotNull(type) {
                "Unable to create ${Element::class.simpleName} without type"
            }

            val id = jsonObject[Constants.ID]?.asStringOrNull() ?: "${parentId ?: ""}${type}_$index"

            val nestedElements = jsonObject[Constants.NESTED_ELEMENTS]
                ?.asJSONArrayOrNull()
                ?.mapIndexedNotNull { nestedIndex, nested ->
                    if (nested is JsonObject) resolveProcessedElement(nested, json, nestedIndex, id) else null
                } ?: emptyList()

            val commonElementProperties = jsonObject.toCommonElementProperties(json)

            // Actions used to be dropped on this path, so an element that declared an action but no
            // processors or constants silently lost it. There is no data to resolve against here,
            // so actions are built from their own JSON.
            val actions = getActions(json, jsonObject).map { it.process(JsonNull, json) }

            return json.kodicesContext.registry
                .elementBuilder(type)
                ?.let { builder ->
                    builder(
                        type,
                        id,
                        jsonObject.asMap().toMutableMap(),
                        nestedElements,
                        actions,
                        json,
                    )
                } ?: ProcessedElement(
                type = type,
                id = id,
                index = index,
                nestedElements = nestedElements,
                text = commonElementProperties.text,
                textSecondary = commonElementProperties.textSecondary,
                actions = actions,
                jsonValues = jsonObject.asMap().toMutableMap(),
                style = commonElementProperties.style,
                validation = commonElementProperties.validation,
                enabled = commonElementProperties.enabled,
                visible = commonElementProperties.visible,
                requiresValidElements = commonElementProperties.requiresValidElements,
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
                json.kodicesContext.logger.warn("An element provided both ${Constants.ACTION} and ${Constants.ACTIONS}. That is usually a mistake. ${Constants.ACTIONS} will be used.")
            }

            val actionsValue = jsonObject[Constants.ACTIONS] ?: jsonObject[Constants.ACTION]

            return when (actionsValue) {
                is JsonArray -> {
                    actionsValue.map { actionJsonContent ->
                        InterimAction(
                            actionJsonContent
                                .asJSONObjectOrNull()
                                ?.get(Constants.TYPE)
                                ?.asStringOrNull() ?: "invalid",
                            actionJsonContent,
                        )
                    }
                }

                is JsonObject -> {
                    listOf(
                        InterimAction(
                            actionsValue.asJSONObjectOrNull()?.get(Constants.TYPE)?.asStringOrNull()
                                ?: "invalid",
                            actionsValue,
                        ),
                    )
                }

                else -> {
                    if (actionsValue != null) {
                        json.kodicesContext.logger.warn(
                            "Invalid type found for ${Action::class.simpleName}. It must be either an object or an array, provided value: $actionsValue",
                        )
                    }
                    emptyList()
                }
            }
        }
    }
}

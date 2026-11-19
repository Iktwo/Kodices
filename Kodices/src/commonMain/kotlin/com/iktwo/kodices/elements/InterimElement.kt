package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.InterimAction
import com.iktwo.kodices.dataprocessors.DataProcessor
import com.iktwo.kodices.kodicesContext
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlin.let

public typealias ProcessedValues = MutableMap<String, JsonElement?>

public data class InterimElement(
    override val type: String,
    override val nestedElements: List<Element> = emptyList(),
    val id: String? = null,
    val constants: JsonObject? = null,
    val processors: Map<String, List<DataProcessor>>? = null,
    val processorsForExpansion: List<DataProcessor> = emptyList(),
    override val actions: List<InterimAction> = emptyList(),
    val action: InterimAction? = null,
    /**
     * Rendered in place of this element when [processorsForExpansion] produce an empty array or
     * nothing at all: a list's "nothing here" line. It is processed with the same data as this
     * element, not with an entry of the array.
     */
    val emptyElement: Element? = null,
) : Element {
    public fun process(
        index: Int = 0,
        data: JsonElement? = null,
        parentId: String? = null,
        json: Json,
    ): List<ProcessedElement> {
        // If there are processors for expansion, then the result of executing them should be an array
        if (processorsForExpansion.isNotEmpty()) {
            var processedData = data

            processorsForExpansion.forEach { dataProcessor ->
                dataProcessor.process(processedData)?.let {
                    processedData = it
                } ?: run {
                    return processEmptyElement(index, data, parentId, json)
                }
            }

            return if (processedData is JsonArray) {
                if (processedData.jsonArray.isEmpty()) {
                    return processEmptyElement(index, data, parentId, json)
                }

                processedData
                    .jsonArray
                    .mapIndexed { repeatingIndex, jsonElement ->
                        // Create an interim element for every array element, but do not set processors for expansion.
                        // A declared id has to be suffixed, otherwise every expanded copy would share it and
                        // renderers keyed on the id (list keys, input state) would collide.
                        val repeatingId = id?.let { "${it}_$repeatingIndex" }
                        // Its actions go with it: they are processed with the entry, like its values.
                        val repeatingElement = InterimElement(
                            type = type,
                            nestedElements = nestedElements,
                            id = repeatingId,
                            constants = constants,
                            processors = processors,
                            processorsForExpansion = emptyList(),
                            actions = actions,
                            action = action,
                        )

                        // Process element with element from the array
                        repeatingElement.process(
                            repeatingIndex,
                            jsonElement,
                            "${if (parentId != null) "${parentId}_" else "${type}_"}index$index",
                            json,
                        )
                    }
                    .flatten()
                    .toList()
            } else {
                json.kodicesContext.logger.warn(
                    "${InterimElement::class.simpleName} expansion failed. $processedData is not a ${JsonArray::class.simpleName}. processedData: $processedData",
                )
                emptyList()
            }
        }

        var processedText: String? = null
        var processedTextSecondary: String? = null
        var style: String? = null

        val processedValues: ProcessedValues = mutableMapOf()

        processors?.forEach { (key, listOfProcessors) ->
            var processingData = data
            listOfProcessors.forEach { processingData = it.process(processingData) }

            processedValues[key] = processingData
        }

        // Constants will replace processors if both are provided
        constants?.forEach { (key, value) ->
            processedValues[key] = value
        }

        processedValues.forEach { (key, processedValue) ->
            // Assign known keys
            if (key in Constants.GENERIC_ELEMENT_KEYS) {
                when (key) {
                    Constants.TEXT_KEY -> {
                        processedValue?.asStringOrNull()?.let {
                            processedText = it
                        } ?: run {
                            json.kodicesContext.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }

                    Constants.TEXT_SECONDARY_KEY -> {
                        processedValue?.asStringOrNull()?.let {
                            processedTextSecondary = it
                        } ?: run {
                            json.kodicesContext.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }

                    Constants.STYLE -> {
                        processedValue?.asStringOrNull()?.let {
                            style = it
                        } ?: run {
                            json.kodicesContext.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }

                    // ENABLED_KEY, VISIBLE_KEY, VALIDATION_KEY and REQUIRES_VALID_ELEMENTS_KEY need no
                    // special handling here: they stay in processedValues and are read below by
                    // toCommonElementProperties.
                }
            }
        }

        // When id is not provided use the type and index
        val processedId = id ?: "${parentId ?: ""}${type}_$index"

        val nestedElements = nestedElements
            .mapIndexed { nestedIndex, element ->
                when (element) {
                    is ProcessedElement -> {
                        listOf(element)
                    }

                    is InterimElement -> {
                        element.process(nestedIndex, data, processedId, json).toList()
                    }
                }
            }.flatten()

        if (action != null && actions.isNotEmpty()) {
            json.kodicesContext.logger.warn(
                "${InterimElement::class.simpleName} $type provided both an action and a list of actions. That is not recommended. The single action will be appended to the actions.",
            )
        }

        val processedActions = actions.plus(action).filterNotNull().map {
            it.process(data ?: JsonNull, json)
        }

        val commonElementProperties = processedValues.toCommonElementProperties(json)

        // If there is a custom element builder, use it, otherwise create a ProcessedElement
        return json.kodicesContext.registry.elementBuilder(type)?.let { builder ->
            listOf(
                builder(
                    type,
                    processedId,
                    processedValues,
                    nestedElements,
                    processedActions,
                    json,
                ),
            )
        } ?: listOf(
            ProcessedElement(
                type = type,
                nestedElements = nestedElements,
                id = processedId,
                index = index,
                text = processedText,
                textSecondary = processedTextSecondary,
                actions = processedActions,
                jsonValues = processedValues.toMap(),
                style = style,
                validation = commonElementProperties.validation,
                enabled = commonElementProperties.enabled,
                visible = commonElementProperties.visible,
                requiresValidElements = commonElementProperties.requiresValidElements,
            ),
        )
    }

    private fun processEmptyElement(
        index: Int,
        data: JsonElement?,
        parentId: String?,
        json: Json,
    ): List<ProcessedElement> =
        when (emptyElement) {
            null -> emptyList()
            is ProcessedElement -> listOf(emptyElement)
            is InterimElement -> emptyElement.process(index, data, "${parentId ?: ""}${Element.EMPTY_ELEMENT_ID_PREFIX}", json)
        }
}

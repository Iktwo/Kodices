package com.iktwo.kodices.elements

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.InterimAction
import com.iktwo.kodices.dataprocessors.DataProcessor
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

typealias ProcessedValues = MutableMap<String, JsonElement?>

data class InterimElement(
    override val type: String,
    override val nestedElements: List<Element> = emptyList(),
    val id: String? = null,
    val constants: JsonObject? = null,
    val processors: Map<String, List<DataProcessor>>? = null,
    val processorsForExpansion: List<DataProcessor> = emptyList(),
    override val actions: List<InterimAction> = emptyList(),
    val action: InterimAction? = null,
) : Element {
    fun process(
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
                    return emptyList()
                }
            }

            return if (processedData is JsonArray) {
                processedData
                    ?.jsonArray
                    ?.mapIndexed { repeatingIndex, jsonElement ->
                        // Create an interim element for every array element, but do not set processors for expansion
                        val repeatingElement = InterimElement(type, nestedElements, id, constants, processors, emptyList())

                        // Process element with element from the array
                        repeatingElement.process(
                            repeatingIndex,
                            jsonElement,
                            "${if (parentId != null) "${parentId}_" else "${type}_"}index$index",
                            json,
                        )
                    }?.flatten()
                    ?.toList() ?: emptyList()
            } else {
                KodicesParser.logger.warn(
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
                            KodicesParser.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }

                    Constants.TEXT_SECONDARY_KEY -> {
                        processedValue?.asStringOrNull()?.let {
                            processedTextSecondary = it
                        } ?: run {
                            KodicesParser.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }

                    Constants.STYLE -> {
                        processedValue?.asStringOrNull()?.let {
                            style = it
                        } ?: run {
                            KodicesParser.logger.warn("${InterimElement::class.simpleName} $key was provided but processed value is not as expected $processedValue")
                        }
                    }
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
            KodicesParser.logger.warn(
                "${InterimElement::class.simpleName} $type provided both an action and a list of actions. That is not recommended. The single action will be appended to the actions.",
            )
        }

        val processedActions = actions.plus(action).filterNotNull().map {
            it.process(data ?: JsonNull)
        }

        val commonElementProperties = processedValues.toCommonElementProperties(json)

        // If there is a custom element builder, use it, otherwise create a ProcessedElement
        return ElementRegistry.getElement(type)?.let { builder ->
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
                jsonValues = processedValues,
                style = style,
                validation = commonElementProperties.validation,
                enabled = commonElementProperties.enabled,
                requiresValidElements = commonElementProperties.requiresValidElements,
            ),
        )
    }
}

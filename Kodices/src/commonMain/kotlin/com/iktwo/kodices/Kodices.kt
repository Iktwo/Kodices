package com.iktwo.kodices

import com.iktwo.kodices.actions.ActionDescriptor
import com.iktwo.kodices.actions.ActionsRegistry
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.content.InterimContent
import com.iktwo.kodices.elements.DefaultInputElements
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ElementRegistry
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class Kodices(
    elements: List<ElementDescriptor> = listOf(),
    actions: List<ActionDescriptor> = listOf()
) {
    private val json: Json = Json { ignoreUnknownKeys = true }

    init {
        val inputElements = DefaultInputElements.map { elementType ->
            object : ElementDescriptor {
                override val type = elementType
                override val builder = InputElement.builder
            }
        }.toList()
        ElementRegistry.addElements(inputElements.plus(elements))

        val defaultActions = listOf(MessageAction.descriptor)
        ActionsRegistry.addActions(defaultActions.plus(actions))
    }

    fun parseJSONElementToContent(
        jsonElement: JsonElement,
        data: JsonElement? = null,
    ): Content? {
        return try {
            val interimContent = json.decodeFromJsonElement(InterimContent.Companion, jsonElement)
            interimContent.process(data, json)
        } catch (e: Exception) {
            if (debug) {
                println("Exception $e at parseJSONToContent")
            }
            null
        }
    }

    fun parseJSONToContent(
        jsonString: String,
        data: JsonElement? = null,
    ): Content? {
        if (jsonString.isBlank()) {
            return null
        }

        return try {
            val interimContent = json.decodeFromString(InterimContent.Companion, jsonString)
            interimContent.process(data, json)
        } catch (e: Exception) {
            if (debug) {
                println("Exception $e at parseJSONToContent. Source json: $jsonString")
            }
            null
        }
    }

    fun parseJSONToContent(
        jsonString: String,
        data: String,
    ): Content? {
        return parseJSONToContent(jsonString, if (data.isBlank()) JsonNull else json.parseToJsonElement(data))
    }

    fun parseJSONWithDataToContent(jsonString: String): Content? {
        return try {
            val jsonObject = json.decodeFromString(JsonObject.serializer(), jsonString).jsonObject
            val content = jsonObject[Constants.CONTENT]
            return parseJSONToContent(content.toString(), jsonObject[Constants.DATA])
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        var debug: Boolean = false

        private val defaultLogger = object : Logger {
            override fun debug(message: String) {
                println("D: $message")
            }

            override fun info(message: String) {
                println("I: $message")
            }

            override fun warn(message: String) {
                println("W: $message")
            }

            override fun error(message: String) {
                println("E: $message")
            }
        }

        var logger: Logger = defaultLogger
    }
}

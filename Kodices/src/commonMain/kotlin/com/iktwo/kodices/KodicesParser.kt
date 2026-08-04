// The legacy constructor dual-writes into the deprecated global registries on purpose, so that
// consumers building one configured parser and a bare one elsewhere keep working.
@file:Suppress("DEPRECATION")

package com.iktwo.kodices

import com.iktwo.kodices.actions.ActionDescriptor
import com.iktwo.kodices.actions.ActionsRegistry
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.content.InterimContent
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ElementRegistry
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Parses JSON UI definitions into a [Content] model.
 *
 * Element, action and data processor types are resolved against this parser's own [registry]. Two
 * parsers built with different descriptors do not see each other's types.
 */
public class KodicesParser {
    /** The types this parser can resolve. */
    public val registry: KodicesRegistry

    private val context: KodicesContext
    private val json: Json

    /**
     * Creates a parser that registers [elements] and [actions] on top of the built-in types.
     *
     * For back-compatibility the descriptors are also written into the deprecated global
     * registries; that dual-write is removed in a future release.
     */
    public constructor(
        elements: List<ElementDescriptor> = listOf(),
        actions: List<ActionDescriptor> = listOf(),
    ) : this(
        registry = KodicesRegistry.of(elements = elements, actions = actions),
    ) {
        @Suppress("DEPRECATION")
        ElementRegistry.addElements(elements)

        @Suppress("DEPRECATION")
        ActionsRegistry.addActions(actions)
    }

    /**
     * Creates a parser backed by an explicit [registry].
     *
     * @param registry the types this parser resolves, see [KodicesRegistry.of].
     * @param json the [Json] used for parsing. A copy carrying this parser's context is made from it.
     * @param logger where parse failures and registry warnings are reported.
     * @param debug whether to log parse failures.
     */
    public constructor(
        registry: KodicesRegistry,
        json: Json = defaultJson,
        logger: Logger = Companion.logger,
        debug: Boolean = Companion.debug,
    ) {
        this.registry = registry
        this.context = KodicesContext(registry = registry, logger = logger, debug = debug)
        this.json = json.withKodicesContext(context)
    }

    /**
     * Parses a [JsonElement] into a [Content] object.
     *
     * @param jsonElement the UI definition.
     * @param data optional data the definition's processors run against.
     * @return the parsed [Content], or null if parsing fails.
     */
    public fun parseJSONElementToContent(
        jsonElement: JsonElement,
        data: JsonElement? = null,
    ): Content? {
        return try {
            val interimContent = json.decodeFromJsonElement(InterimContent.Companion, jsonElement)
            interimContent.process(data, json)
        } catch (e: Exception) {
            logFailure("parseJSONElementToContent", e, jsonElement.toString())
            null
        }
    }

    /**
     * Parses a JSON string into a [Content] object.
     *
     * @param jsonString The JSON string to parse.
     * @param data An optional [JsonElement] representing additional data to be used during parsing.
     * @return A [Content] object representing the parsed UI model, or null if parsing fails.
     */
    public fun parseJSONToContent(
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
            logFailure("parseJSONToContent", e, jsonString)
            null
        }
    }

    /**
     * Parses a JSON string into a [Content] object, using [data] as the data source.
     *
     * @return A [Content] object, or null if parsing fails.
     */
    public fun parseJSONToContent(
        jsonString: String,
        data: String,
    ): Content? {
        return parseJSONToContent(jsonString, if (data.isBlank()) JsonNull else json.parseToJsonElement(data))
    }

    /**
     * Parses a JSON string holding both the UI definition and its data, as
     * `{"content": ..., "data": ...}`.
     *
     * @return A [Content] object, or null if parsing fails.
     */
    public fun parseJSONWithDataToContent(jsonString: String): Content? {
        return try {
            val jsonObject = json.decodeFromString(JsonObject.serializer(), jsonString).jsonObject
            val content = jsonObject[Constants.CONTENT]
            parseJSONToContent(content.toString(), jsonObject[Constants.DATA])
        } catch (e: Exception) {
            logFailure("parseJSONWithDataToContent", e, jsonString)
            null
        }
    }

    private fun logFailure(
        where: String,
        e: Exception,
        source: String,
    ) {
        // Routed through the context's logger rather than println, so consumers can capture it.
        if (context.debug) {
            context.logger.error("Exception $e at $where. Source json: $source")
        } else {
            context.logger.error("Exception $e at $where")
        }
    }

    public companion object Companion {
        internal val defaultJson: Json = Json { ignoreUnknownKeys = true }

        @Deprecated(
            "Global state. Pass debug to the KodicesParser(registry, json, logger, debug) constructor instead.",
        )
        public var debug: Boolean = false

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

        @Deprecated(
            "Global state. Pass logger to the KodicesParser(registry, json, logger, debug) constructor instead.",
        )
        public var logger: Logger = defaultLogger
    }
}

// This file is the compatibility bridge to the deprecated global registries, so it references
// them on purpose.
@file:Suppress("DEPRECATION")

package com.iktwo.kodices

import com.iktwo.kodices.actions.ActionBuilder
import com.iktwo.kodices.actions.ActionDescriptor
import com.iktwo.kodices.actions.ActionsRegistry
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.dataprocessors.DataProcessorBuilder
import com.iktwo.kodices.dataprocessors.DataProcessorRegistry
import com.iktwo.kodices.dataprocessors.JSONDrillerProcessor
import com.iktwo.kodices.dataprocessors.StringProcessor
import com.iktwo.kodices.dataprocessors.StylerProcessor
import com.iktwo.kodices.elements.DefaultInputElements
import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ElementRegistry
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.utils.Logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

/**
 * The element, action and data processor builders a single [KodicesParser] resolves against.
 *
 * This is an immutable snapshot taken when the parser is constructed. Two parsers configured with
 * different element sets no longer see each other's types, which was the case while the registries
 * were process-global.
 */
public class KodicesRegistry internal constructor(
    private val elements: Map<String, ElementBuilder>,
    private val actions: Map<String, ActionBuilder>,
    private val processors: Map<String, DataProcessorBuilder>,
    private val allowGlobalFallback: Boolean,
    private val logger: Logger,
) {
    public fun elementBuilder(type: String): ElementBuilder? =
        elements[type] ?: fallback(type) {
            @Suppress("DEPRECATION")
            ElementRegistry.getElement(type)
        }

    public fun actionBuilder(type: String): ActionBuilder? =
        actions[type] ?: fallback(type) {
            @Suppress("DEPRECATION")
            ActionsRegistry.getAction(type)
        }

    public fun dataProcessorBuilder(type: String): DataProcessorBuilder? =
        processors[type] ?: fallback(type) {
            @Suppress("DEPRECATION")
            DataProcessorRegistry.processors[type]
        }

    private inline fun <T : Any> fallback(
        type: String,
        lookup: () -> T?,
    ): T? {
        if (!allowGlobalFallback) return null

        return lookup()?.also {
            logger.warn(
                "\"$type\" was resolved from the deprecated global registry. Register it on the " +
                    "KodicesParser instance instead; the global registries are removed in 1.0.",
            )
        }
    }

    public companion object {
        /**
         * Builds a registry.
         *
         * @param elements additional element descriptors.
         * @param actions additional action descriptors.
         * @param processors additional data processor builders, keyed by type.
         * @param includeDefaults whether to register the built-in input elements, the built-in
         * [MessageAction] and the built-in data processors.
         * @param allowGlobalFallback whether a type missing from this registry may still be resolved
         * from the deprecated global registries. On by default so existing consumers keep working;
         * every fallback hit is logged as a warning.
         * @param logger used to report fallback hits.
         */
        public fun of(
            elements: List<ElementDescriptor> = emptyList(),
            actions: List<ActionDescriptor> = emptyList(),
            processors: Map<String, DataProcessorBuilder> = emptyMap(),
            includeDefaults: Boolean = true,
            allowGlobalFallback: Boolean = true,
            logger: Logger = KodicesParser.logger,
        ): KodicesRegistry {
            val defaultElements = if (includeDefaults) {
                DefaultInputElements.associateWith { InputElement.builder }
            } else {
                emptyMap()
            }

            val defaultActions = if (includeDefaults) {
                mapOf(MessageAction.descriptor.type to MessageAction.descriptor.builder)
            } else {
                emptyMap()
            }

            val defaultProcessors = if (includeDefaults) DEFAULT_PROCESSORS else emptyMap()

            return KodicesRegistry(
                elements = defaultElements + elements.associate { it.type to it.builder },
                actions = defaultActions + actions.associate { it.type to it.builder },
                processors = defaultProcessors + processors,
                allowGlobalFallback = allowGlobalFallback,
                logger = logger,
            )
        }

        internal val DEFAULT_PROCESSORS: Map<String, DataProcessorBuilder> = mapOf(
            JSONDrillerProcessor.TYPE to { json, jsonObject ->
                json.decodeFromJsonElement(JSONDrillerProcessor.Companion, jsonObject)
            },
            StringProcessor.TYPE to { json, jsonObject ->
                json.decodeFromJsonElement(StringProcessor.serializer(), jsonObject)
            },
            StylerProcessor.TYPE to { json, jsonObject ->
                json.decodeFromJsonElement(StylerProcessor.serializer(), jsonObject)
            },
        )
    }
}

/**
 * Everything a parse needs that used to live in global state: the [registry] to resolve types
 * against, and where to log.
 */
internal class KodicesContext(
    val registry: KodicesRegistry,
    val logger: Logger,
    val debug: Boolean,
)

/**
 * Carries a [KodicesContext] on a [Json] instance.
 *
 * The serializers for [com.iktwo.kodices.elements.Element], [com.iktwo.kodices.actions.Action] and
 * [com.iktwo.kodices.dataprocessors.DataProcessor] are reached through `KSerializer.deserialize`,
 * which has no parameter to pass a registry through - but it always has a `JsonDecoder`, and so the
 * `Json` instance that started the parse. Riding along in that instance's `SerializersModule` is
 * what makes the registry per-parser instead of global.
 *
 * It is never actually used to serialize anything; the descriptor exists only because `Json`
 * validates its module at construction time.
 */
internal class ContextCarrier(
    val context: KodicesContext,
) : KSerializer<KodicesContext> {
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor = buildClassSerialDescriptor("com.iktwo.kodices.KodicesContext")

    override fun serialize(
        encoder: Encoder,
        value: KodicesContext,
    ): Nothing = error("${KodicesContext::class.simpleName} is not serializable")

    override fun deserialize(decoder: Decoder): Nothing = error("${KodicesContext::class.simpleName} is not serializable")
}

/**
 * The context attached to this [Json], or a context backed by the deprecated global registries when
 * the `Json` did not come from a [KodicesParser].
 */
@OptIn(ExperimentalSerializationApi::class)
internal val Json.kodicesContext: KodicesContext
    get() = (serializersModule.getContextual(KodicesContext::class) as? ContextCarrier)?.context
        ?: legacyGlobalContext()

/** Returns a copy of this [Json] that carries [context]. */
internal fun Json.withKodicesContext(context: KodicesContext): Json =
    Json(from = this) {
        serializersModule = this@withKodicesContext.serializersModule +
            SerializersModule { contextual(KodicesContext::class, ContextCarrier(context)) }
    }

/**
 * Fallback for a `Json` that was not produced by a [KodicesParser], for example
 * `Constants.json.decodeFromJsonElement<Element>(...)`. Resolves purely against the deprecated
 * global registries.
 */
private fun legacyGlobalContext(): KodicesContext =
    KodicesContext(
        registry = KodicesRegistry.of(
            includeDefaults = false,
            allowGlobalFallback = true,
            logger = KodicesParser.logger,
        ),
        logger = KodicesParser.logger,
        debug = KodicesParser.debug,
    )

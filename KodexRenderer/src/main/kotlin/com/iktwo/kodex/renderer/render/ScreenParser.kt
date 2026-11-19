package com.iktwo.kodex.renderer.render

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.KodicesRegistry
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.Logger
import com.iktwo.piktographs.navigation.DismissModalAction
import com.iktwo.piktographs.navigation.ModalAction
import com.iktwo.piktographs.navigation.NavigateAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Turns screen JSON into Kodices [Content].
 *
 * A screen is either a Kodices content object (`{"elements": [...]}`) or an envelope
 * `{"content": {...}, "data": {...}}`. Either way [ScreenData] (`now`) is merged into the data the
 * processors run against.
 */
internal class ScreenParser(
    private val screenData: ScreenData,
) {
    // Registered explicitly, like the sample app: a type nobody registered is a bug in the screen,
    // not something to resolve from the deprecated global registry. Actions are inert in an image
    // but registered so screens shared with interactive clients still parse.
    private val parser = KodicesParser(
        registry = KodicesRegistry.of(
            actions = listOf(
                MessageAction.descriptor,
                NavigateAction.descriptor,
                ModalAction.descriptor,
                DismissModalAction.descriptor,
            ),
            allowGlobalFallback = false,
            logger = KodicesSlf4jLogger,
        ),
        logger = KodicesSlf4jLogger,
        debug = true,
    )

    /** @throws ScreenParseException if [screenJson] is not valid Kodices content. */
    fun parse(screenJson: String): Content {
        val resolved = resolve(screenJson)

        return parser.parseJSONElementToContent(resolved.content, resolved.data)
            ?: throw ScreenParseException("Screen JSON is not valid Kodices content")
    }

    /**
     * Splits [screenJson] into its content and the data its processors run against, with `now`
     * merged in. This is what a client that renders the screen itself needs.
     *
     * @throws ScreenParseException if [screenJson] is not JSON.
     */
    fun resolve(screenJson: String): ResolvedScreen {
        val root = try {
            Json.parseToJsonElement(screenJson)
        } catch (e: Exception) {
            throw ScreenParseException("Screen is not valid JSON: ${e.message}")
        }

        val (contentJson, data) = if (root is JsonObject && root.containsKey(Constants.CONTENT)) {
            root.getValue(Constants.CONTENT) to root[Constants.DATA]
        } else {
            root to null
        }

        return ResolvedScreen(contentJson, screenData.merge(data))
    }
}

/** A screen's Kodices content and the data to process it with. */
public class ResolvedScreen(
    public val content: JsonElement,
    public val data: JsonObject,
)

private object KodicesSlf4jLogger : Logger {
    private val log = LoggerFactory.getLogger("com.iktwo.kodices")

    override fun debug(message: String) = log.debug(message)

    override fun info(message: String) = log.info(message)

    override fun warn(message: String) = log.warn(message)

    override fun error(message: String) = log.error(message)
}

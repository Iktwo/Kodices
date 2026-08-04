package com.iktwo.kodices

import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.Logger
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A registry is per-parser. These tests exist because it used to be process-global, so a second
 * parser silently inherited the first one's element types.
 */
class KodicesRegistryTest {
    private class MarkerElement(
        id: String,
        val marker: String,
    ) : ProcessedElement(type = "marked", id = id, text = marker)

    private fun descriptor(marker: String) =
        object : ElementDescriptor {
            override val type = "marked"
            override val builder: ElementBuilder = { _, elementId, _, _, _, _ ->
                MarkerElement(elementId, marker)
            }
        }

    private class RecordingLogger : Logger {
        val warnings = mutableListOf<String>()

        override fun debug(message: String) = Unit

        override fun info(message: String) = Unit

        override fun warn(message: String) {
            warnings += message
        }

        override fun error(message: String) = Unit
    }

    private val markedContent = buildJsonObject {
        putJsonArray(Constants.ELEMENTS) {
            addJsonObject {
                put(Constants.TYPE, "marked")
                put(Constants.ID, "target")
            }
        }
    }

    @Test
    fun `Two parsers resolve the same type against their own registry`() {
        val first = KodicesParser(
            registry = KodicesRegistry.of(elements = listOf(descriptor("first")), allowGlobalFallback = false),
        )
        val second = KodicesParser(
            registry = KodicesRegistry.of(elements = listOf(descriptor("second")), allowGlobalFallback = false),
        )

        val fromFirst = first.parseJSONElementToContent(markedContent)?.elements?.single()
        val fromSecond = second.parseJSONElementToContent(markedContent)?.elements?.single()

        assertTrue(fromFirst is MarkerElement)
        assertTrue(fromSecond is MarkerElement)
        assertEquals("first", fromFirst.marker)
        assertEquals("second", fromSecond.marker)
    }

    @Test
    fun `A parser without a descriptor does not see another parser's type`() {
        KodicesParser(
            registry = KodicesRegistry.of(elements = listOf(descriptor("configured")), allowGlobalFallback = false),
        )

        val bare = KodicesParser(registry = KodicesRegistry.of(allowGlobalFallback = false))

        val element = bare.parseJSONElementToContent(markedContent)?.elements?.single()

        assertNotNull(element)
        assertTrue(element !is MarkerElement, "The bare parser must not resolve another parser's element type")
        assertEquals("marked", element.type)
    }

    @Test
    fun `Built-in input elements and processors are registered by default`() {
        val parser = KodicesParser(registry = KodicesRegistry.of(allowGlobalFallback = false))

        assertNotNull(parser.registry.elementBuilder("textInput"))
        assertNotNull(parser.registry.elementBuilder("checkbox"))
        assertNotNull(parser.registry.elementBuilder("textArea"))
        assertNotNull(parser.registry.actionBuilder("message"))
        assertNotNull(parser.registry.dataProcessorBuilder("string"))
        assertNotNull(parser.registry.dataProcessorBuilder("path"))
        assertNotNull(parser.registry.dataProcessorBuilder("styler"))
    }

    @Test
    fun `includeDefaults false registers nothing`() {
        val registry = KodicesRegistry.of(includeDefaults = false, allowGlobalFallback = false)

        assertEquals(null, registry.elementBuilder("textInput"))
        assertEquals(null, registry.actionBuilder("message"))
        assertEquals(null, registry.dataProcessorBuilder("string"))
    }

    @Test
    fun `The legacy constructor still registers globally and warns when the fallback is used`() {
        // The deprecated dual-write is what keeps consumers working who build one configured parser
        // and a bare one elsewhere.
        KodicesParser(elements = listOf(descriptor("legacy")))

        val logger = RecordingLogger()
        val bare = KodicesParser(
            registry = KodicesRegistry.of(allowGlobalFallback = true, logger = logger),
            logger = logger,
        )

        val element = bare.parseJSONElementToContent(markedContent)?.elements?.single()

        assertTrue(element is MarkerElement, "The global fallback should still resolve the type")
        assertEquals("legacy", element.marker)
        assertTrue(
            logger.warnings.any { it.contains("marked") && it.contains("global registry") },
            "Using the fallback must warn, got ${logger.warnings}",
        )
    }
}

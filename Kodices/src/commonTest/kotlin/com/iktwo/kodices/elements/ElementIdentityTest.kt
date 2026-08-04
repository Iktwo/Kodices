package com.iktwo.kodices.elements

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.dataprocessors.JSONDrillerProcessor
import com.iktwo.kodices.utils.Constants
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Element ids are used as list keys by renderers, so they have to be unique within a [com.iktwo.kodices.content.Content].
 */
class ElementIdentityTest {
    private val parser = KodicesParser()

    @Test
    fun `Elements without an id get distinct ids`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject { put(Constants.TYPE, "row") }
                    addJsonObject { put(Constants.TYPE, "row") }
                    addJsonObject { put(Constants.TYPE, "separator") }
                }
            },
        )

        assertNotNull(content)
        assertEquals(3, content.elements.size)

        val ids = content.elements.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Element ids must be unique, got $ids")
        assertFalse(ids.contains("id"), "The literal string \"id\" is not a usable generated id, got $ids")
    }

    @Test
    fun `Nested elements without an id get distinct ids`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        putJsonArray(Constants.NESTED_ELEMENTS) {
                            addJsonObject { put(Constants.TYPE, "row") }
                            addJsonObject { put(Constants.TYPE, "row") }
                        }
                    }
                }
            },
        )

        assertNotNull(content)

        val nestedIds = content.elements
            .first()
            .nestedElements
            .map { it.id }
        assertEquals(2, nestedIds.size)
        assertEquals(nestedIds.size, nestedIds.toSet().size, "Nested element ids must be unique, got $nestedIds")
    }

    @Test
    fun `An explicit id is preserved`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "chosen")
                    }
                }
            },
        )

        assertNotNull(content)
        assertEquals("chosen", content.elements.first().id)
    }

    @Test
    fun `Expanded elements get distinct ids even when the source declares one`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "repeated")

                        putJsonObject(Constants.PROCESSORS) {
                            putJsonObject(Constants.TEXT_KEY) {
                                put(Constants.TYPE, JSONDrillerProcessor.TYPE)
                                put(Constants.ELEMENT, "name")
                            }
                        }

                        putJsonObject(Constants.EXPAND_WITH_PROCESSOR) {
                            put(Constants.TYPE, JSONDrillerProcessor.TYPE)
                        }
                    }
                }
            },
            buildJsonArray {
                addJsonObject { put("name", "first") }
                addJsonObject { put("name", "second") }
                addJsonObject { put("name", "third") }
            },
        )

        assertNotNull(content)
        assertEquals(3, content.elements.size)
        assertEquals(listOf("first", "second", "third"), content.elements.map { it.text })

        val ids = content.elements.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Expanded element ids must be unique, got $ids")
        assertTrue(ids.all { it.startsWith("repeated") }, "Expanded ids should keep the declared id as a prefix, got $ids")
    }

    @Test
    fun `visible is honored on an element without processors or constants`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "hidden")
                        put(Constants.VISIBLE_KEY, false)
                    }
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "disabled")
                        put(Constants.ENABLED_KEY, false)
                    }
                }
            },
        )

        assertNotNull(content)

        val hidden = content.elements.first { it.id == "hidden" }
        assertFalse(hidden.visible, "\"visible\": false must be honored")
        assertTrue(hidden.enabled, "enabled defaults to true")

        val disabled = content.elements.first { it.id == "disabled" }
        assertFalse(disabled.enabled, "\"enabled\": false must be honored")
        assertTrue(disabled.visible, "visible defaults to true")
    }

    @Test
    fun `visible and enabled are honored on an element built from constants`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "hidden")

                        putJsonObject(Constants.CONSTANTS) {
                            put(Constants.TEXT_KEY, "Test row")
                            put(Constants.VISIBLE_KEY, false)
                            put(Constants.ENABLED_KEY, false)
                        }
                    }
                }
            },
        )

        assertNotNull(content)

        val element = content.elements.single()
        assertEquals("Test row", element.text)
        assertFalse(element.visible, "\"visible\": false must be honored on the interim path")
        assertFalse(element.enabled, "\"enabled\": false must be honored on the interim path")
    }

    @Test
    fun `Elements are rendered in source order`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    add(
                        buildJsonObject {
                            put(Constants.TYPE, "row")
                            put(Constants.TEXT_KEY, "first")
                        },
                    )
                    add(
                        buildJsonObject {
                            put(Constants.TYPE, "row")
                            put(Constants.TEXT_KEY, "second")
                        },
                    )
                }
            },
        )

        assertNotNull(content)
        assertEquals(listOf("first", "second"), content.elements.map { it.text })
    }
}

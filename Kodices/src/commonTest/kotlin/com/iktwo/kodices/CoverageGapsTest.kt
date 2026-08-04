package com.iktwo.kodices

import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.actions.MessageStyle
import com.iktwo.kodices.actions.SimpleAction
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asMap
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers public API that previously had no tests at all.
 */
class CoverageGapsTest {
    private val parser = KodicesParser()

    // region parseJSONWithDataToContent

    @Test
    fun `parseJSONWithDataToContent reads the content and data envelope`() {
        val content = parser.parseJSONWithDataToContent(
            """
            {
              "content": {
                "elements": [
                  {
                    "type": "row",
                    "id": "name",
                    "processors": { "text": { "type": "path", "element": "user" } }
                  }
                ]
              },
              "data": { "user": "Ada" }
            }
            """.trimIndent(),
        )

        assertNotNull(content)
        assertEquals("Ada", content.elements.single().text)
    }

    @Test
    fun `parseJSONWithDataToContent returns null on malformed input`() {
        assertNull(parser.parseJSONWithDataToContent("not json"))
        assertNull(parser.parseJSONWithDataToContent(""))
    }

    // endregion

    // region MessageAction

    @Test
    fun `MessageAction is built from an element's action`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "trigger")
                        putJsonObject(Constants.ACTION) {
                            put(Constants.TYPE, MessageAction.TYPE)
                            put(Constants.STYLE, "toast")
                            putJsonObject(Constants.CONSTANTS) {
                                put(Constants.TEXT_KEY, "Saved")
                            }
                        }
                    }
                }
            },
        )

        assertNotNull(content)

        val action = content.elements.single().actions.single()
        assertTrue(action is MessageAction)
        assertEquals("Saved", action.text)
        assertEquals(MessageStyle.TOAST, action.style)
    }

    @Test
    fun `An unregistered action type degrades to a SimpleAction`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "trigger")
                        putJsonObject(Constants.ACTION) {
                            put(Constants.TYPE, "notRegistered")
                        }
                    }
                }
            },
        )

        assertNotNull(content)

        val action = content.elements.single().actions.single()
        assertTrue(action is SimpleAction)
        assertEquals("notRegistered", action.type)
    }

    @Test
    fun `MessageStyle parsing is case insensitive and defaults to DIALOG`() {
        assertEquals(MessageStyle.TOAST, MessageStyle.fromString("toast"))
        assertEquals(MessageStyle.TOAST, MessageStyle.fromString("TOAST"))
        assertEquals(MessageStyle.SNACK_BAR, MessageStyle.fromString("snack_bar"))
        assertEquals(MessageStyle.DIALOG, MessageStyle.fromString("nonsense"))
        assertEquals(MessageStyle.DIALOG, MessageStyle.fromString(null))
    }

    // endregion

    // region InputElement

    @Test
    fun `Input element types are built as InputElement`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, INPUT_ELEMENT_TEXT_INPUT)
                        put(Constants.ID, "name")
                    }
                    addJsonObject {
                        put(Constants.TYPE, INPUT_ELEMENT_CHECKBOX)
                        put(Constants.ID, "agree")
                    }
                }
            },
        )

        assertNotNull(content)
        assertTrue(content.elements.all { it is InputElement })
    }

    @Test
    fun `An input element without validation is always valid`() {
        val element = parser
            .parseJSONElementToContent(
                buildJsonObject {
                    putJsonArray(Constants.ELEMENTS) {
                        addJsonObject {
                            put(Constants.TYPE, INPUT_ELEMENT_TEXT_INPUT)
                            put(Constants.ID, "name")
                        }
                    }
                },
            )?.elements
            ?.single() as? InputElement

        assertNotNull(element)
        assertTrue(element.isValid)
        assertTrue(element.isValid(""))
        assertTrue(element.isValid("anything"))
    }

    @Test
    fun `An input element with validation checks the text against it`() {
        val element = parser
            .parseJSONElementToContent(
                buildJsonObject {
                    putJsonArray(Constants.ELEMENTS) {
                        addJsonObject {
                            put(Constants.TYPE, INPUT_ELEMENT_TEXT_INPUT)
                            put(Constants.ID, "digits")
                            putJsonObject(Constants.VALIDATION_KEY) {
                                put("regex", "^[0-9]+$")
                            }
                        }
                    }
                },
            )?.elements
            ?.single() as? InputElement

        assertNotNull(element)
        assertTrue(element.isValid("123"))
        assertFalse(element.isValid("abc"))
    }

    // endregion

    // region Element properties

    @Test
    fun `requiresValidElements is read off an element`() {
        val content = parser.parseJSONElementToContent(
            buildJsonObject {
                putJsonArray(Constants.ELEMENTS) {
                    addJsonObject {
                        put(Constants.TYPE, "row")
                        put(Constants.ID, "submit")
                        putJsonArray(Constants.REQUIRES_VALID_ELEMENTS_KEY) {
                            add(JsonPrimitive("name"))
                            add(JsonPrimitive("email"))
                        }
                    }
                }
            },
        )

        assertNotNull(content)
        assertEquals(listOf("name", "email"), content.elements.single().requiresValidElements)
    }

    // endregion

    // region Extensions

    @Test
    fun `asBooleanOrNull reads booleans including quoted ones`() {
        assertEquals(true, JsonPrimitive(true).asBooleanOrNull())
        assertEquals(false, JsonPrimitive(false).asBooleanOrNull())
        // Quoted booleans are accepted, so `"enabled": "false"` behaves like `"enabled": false`.
        assertEquals(true, JsonPrimitive("true").asBooleanOrNull())
        assertNull(JsonPrimitive(1).asBooleanOrNull())
        assertNull(JsonNull.asBooleanOrNull())
    }

    @Test
    fun `asIntOrNull reads integers including quoted ones`() {
        assertEquals(42, JsonPrimitive(42).asIntOrNull())
        assertEquals(-7, JsonPrimitive(-7).asIntOrNull())
        // Quoted integers are accepted too.
        assertEquals(42, JsonPrimitive("42").asIntOrNull())
        assertNull(JsonPrimitive("nope").asIntOrNull())
        assertNull(JsonNull.asIntOrNull())
    }

    @Test
    fun `asMap exposes every key of a JsonObject`() {
        val map = buildJsonObject {
            put("a", "one")
            put("b", 2)
            putJsonObject("c") { put("nested", true) }
        }.asMap()

        assertEquals(setOf("a", "b", "c"), map.keys)
        assertEquals(JsonPrimitive("one"), map["a"])
        assertEquals(JsonPrimitive(2), map["b"])
    }

    // endregion
}

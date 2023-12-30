package com.iktwo.kodices.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExtensionsTest {
    @Test
    fun jsonElementAsJSONObjectOrNull() {
        val element: JsonElement =
            buildJsonObject {
                put("key", "value")
            }

        val jsonObject = element.asJSONObjectOrNull()

        assertNotNull(jsonObject)
        assertEquals("value", jsonObject["key"]?.asString())
    }

    @Test
    fun jsonElementAsStringOrNullWithValidString() {
        val element: JsonElement = JsonPrimitive("test")
        val value = element.asStringOrNull()
        assertEquals("test", value)
    }

    @Test
    fun jsonElementAsStringOrNullWithArray() {
        val element: JsonElement =
            buildJsonArray {
                add("value")
            }
        val value = element.asStringOrNull()
        assertNull(value)
    }

    @Test
    fun jsonElementAsStringOrNullWithObject() {
        val element: JsonElement =
            buildJsonObject {
                put("key", "value")
            }

        val value = element.asStringOrNull()
        assertNull(value)
    }
}

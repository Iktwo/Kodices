package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JSONDrillerProcessorTest {
    @Test
    fun testJSONPathProcessorDeserializationEmptyElements() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\",\"elements\":[]}",
        )
        assertNotNull(jsonDrillerProcessor)
        assertEquals(0, jsonDrillerProcessor.elements.size)
    }

    @Test
    fun testJSONPathProcessorDeserializationOneElement() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"element\":\"key\"}",
        )
        assertNotNull(jsonDrillerProcessor)
        assertEquals(1, jsonDrillerProcessor.elements.size)
    }

    @Test
    fun testJSONPathProcessorDeserializationSingleElements() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"key\"]}",
        )
        assertNotNull(jsonDrillerProcessor)
        assertEquals(1, jsonDrillerProcessor.elements.size)
    }

    @Test
    fun testJSONPathProcessorDeserializationMixedElements() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"key\", 8]}",
        )
        assertNotNull(jsonDrillerProcessor)
        assertEquals(2, jsonDrillerProcessor.elements.size)
        assertEquals(8, (jsonDrillerProcessor.elements[1] as JSONRoute.NumberRoute).value)
    }

    @Test
    fun testJSONPathProcessorProcessNullData() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"key\", 0]}",
        )

        jsonDrillerProcessor.process(JsonNull)
    }

    @Test
    fun testJSONPathProcessorProcessEmpty() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[]}",
        )

        assertEquals(
            "data",
            jsonDrillerProcessor.process(
                buildJsonObject {
                    put("key", "data")
                },
            )?.jsonObject?.get("key")?.asStringOrNull(),
        )
    }

    @Test
    fun testJSONPathProcessorProcessNumber() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[1]}",
        )

        assertEquals(
            1,
            jsonDrillerProcessor.process(
                JsonArray(
                    listOf(
                        JsonPrimitive("0"),
                        JsonPrimitive("1"),
                    ),
                ),
            )?.jsonPrimitive?.double?.toInt(),
        )
    }

    @Test
    fun testJSONPathProcessorProcessString() {
        val jsonDrillerProcessor = Json.decodeFromString(
            JSONDrillerProcessor.Companion,
            "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"key\"]}",
        )

        assertEquals(
            "data",
            jsonDrillerProcessor.process(
                buildJsonObject {
                    put("key", "data")
                },
            )?.asStringOrNull(),
        )
    }

    @Test
    fun testJSONPathProcessorProcessMixed() {
        val jsonDrillerProcessor =
            Json.decodeFromString(
                JSONDrillerProcessor.Companion,
                "{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"key\", 0, \"nested_key\", 1]}",
            )

        assertEquals(
            "data",
            jsonDrillerProcessor.process(
                buildJsonObject {
                    put(
                        "key",
                        buildJsonArray {
                            addJsonObject {
                                put(
                                    "nested_key",
                                    buildJsonArray {
                                        add(true)
                                        add("data")
                                    },
                                )
                            }
                        },
                    )
                },
            )?.asStringOrNull(),
        )
    }
}

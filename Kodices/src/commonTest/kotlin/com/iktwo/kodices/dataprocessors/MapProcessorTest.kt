package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.KodicesParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MapProcessorTest {
    private fun processor(json: String): MapProcessor = Json.decodeFromString(MapProcessor.serializer(), json)

    private val zeroIsNeutral = """{"type":"map","values":{"0":"neutral"},"default":"warning"}"""

    @Test
    fun aListedValueIsReplaced() {
        assertEquals(JsonPrimitive("neutral"), processor(zeroIsNeutral).process(JsonPrimitive(0)))
    }

    @Test
    fun numbersStringsAndBooleansAreLookedUpByTheirText() {
        val processor = processor("""{"type":"map","values":{"0":"zero","true":"yes","WARN":"warning"}}""")

        assertEquals(JsonPrimitive("zero"), processor.process(JsonPrimitive(0)))
        assertEquals(JsonPrimitive("zero"), processor.process(JsonPrimitive("0")))
        assertEquals(JsonPrimitive("yes"), processor.process(JsonPrimitive(true)))
        assertEquals(JsonPrimitive("warning"), processor.process(JsonPrimitive("WARN")))
    }

    @Test
    fun anUnlistedValueBecomesTheDefault() {
        assertEquals(JsonPrimitive("warning"), processor(zeroIsNeutral).process(JsonPrimitive(5)))
    }

    @Test
    fun anUnlistedValuePassesThroughWithoutADefault() {
        val processor = processor("""{"type":"map","values":{"0":"neutral"}}""")

        assertEquals(JsonPrimitive("Online"), processor.process(JsonPrimitive("Online")))
        assertNull(processor.process(null))
    }

    @Test
    fun missingDataIsLookedUpAsNullAndOtherwiseGetsTheDefault() {
        assertEquals(JsonPrimitive("warning"), processor(zeroIsNeutral).process(null))
        assertEquals(JsonPrimitive("warning"), processor(zeroIsNeutral).process(JsonNull))
        assertEquals(
            JsonPrimitive("none"),
            processor("""{"type":"map","values":{"null":"none"},"default":"some"}""").process(null),
        )
    }

    @Test
    fun objectsAndArraysAreNeverListed() {
        val processor = processor(zeroIsNeutral)

        assertEquals(JsonPrimitive("warning"), processor.process(buildJsonObject { }))
        assertEquals(JsonPrimitive("warning"), processor.process(buildJsonArray { }))

        val withoutDefault = processor("""{"type":"map","values":{"0":"neutral"}}""")
        assertEquals(buildJsonArray { }, withoutDefault.process(buildJsonArray { }))
    }

    @Test
    fun aValueCanBeMappedToNothing() {
        assertNull(processor("""{"type":"map","values":{"":null},"default":"set"}""").process(JsonPrimitive("")))
    }

    @Test
    fun itIsRegisteredByDefaultAndChainsAfterAPath() {
        val content = KodicesParser().parseJSONToContent(
            """
            { "elements": [
              {
                "type": "stat",
                "processors": {
                  "value": [{ "type": "path", "elements": ["failures"] }],
                  "tone": [
                    { "type": "path", "elements": ["failures"] },
                    { "type": "map", "values": { "0": "neutral" }, "default": "error" }
                  ]
                }
              }
            ] }
            """.trimIndent(),
            """{ "failures": 3 }""",
        )

        assertEquals(JsonPrimitive("error"), assertNotNull(content).elements.single().jsonValues["tone"])
    }
}

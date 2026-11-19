package com.iktwo.kodices.elements

import com.iktwo.kodices.KodicesParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmptyElementTest {
    private val parser = KodicesParser()

    private val screen = """
        { "elements": [
          { "type": "text", "constants": { "text": "Alerts" } },
          {
            "type": "listItem",
            "expandWithProcessors": [{ "type": "path", "elements": ["alerts"] }],
            "processors": { "text": [{ "type": "path", "elements": ["title"] }] },
            "emptyElement": {
              "type": "text",
              "processors": { "text": [{ "type": "path", "elements": ["nothing"] }] }
            }
          }
        ] }
    """.trimIndent()

    private fun elements(data: String): List<ProcessedElement> = assertNotNull(parser.parseJSONToContent(screen, data)).elements

    @Test
    fun anExpansionWithEntriesDoesNotShowTheEmptyElement() {
        val elements = elements("""{ "alerts": [{ "title": "Disk full" }, { "title": "Fan" }], "nothing": "No alerts" }""")

        assertEquals(listOf("Alerts", "Disk full", "Fan"), elements.map { it.text })
    }

    @Test
    fun anEmptyArrayShowsTheEmptyElementProcessedWithTheParentsData() {
        val elements = elements("""{ "alerts": [], "nothing": "No alerts" }""")

        assertEquals(listOf("text", "text"), elements.map { it.type })
        assertEquals("No alerts", elements[1].text)
    }

    @Test
    fun missingDataShowsTheEmptyElement() {
        assertEquals("No alerts", elements("""{ "nothing": "No alerts" }""")[1].text)
        assertEquals("No alerts", elements("""{ "alerts": null, "nothing": "No alerts" }""")[1].text)
    }

    @Test
    fun dataThatIsNotAnArrayShowsNothing() {
        assertEquals(1, elements("""{ "alerts": "oops", "nothing": "No alerts" }""").size)
    }

    @Test
    fun theEmptyElementKeepsItsIdApartFromItsSiblings() {
        // Both are a "text" and the placeholder takes its parent's position, so without the prefix
        // a placeholder at position 0 would share "text_0" with nothing else only by luck.
        val content = parser.parseJSONToContent(
            """
            { "elements": [
              {
                "type": "text",
                "expandWithProcessors": [{ "type": "path", "elements": ["items"] }],
                "emptyElement": { "type": "text", "text": "Nothing here" }
              },
              { "type": "text", "text": "Footer" }
            ] }
            """.trimIndent(),
            """{ "items": [] }""",
        )

        val elements = assertNotNull(content).elements
        assertEquals(listOf("Nothing here", "Footer"), elements.map { it.text })
        assertEquals(elements.size, elements.map { it.id }.distinct().size)
        assertTrue(elements[0].id.startsWith("empty_"))
    }

    @Test
    fun aNestedExpansionCanHaveAnEmptyElement() {
        val content = parser.parseJSONToContent(
            """
            { "elements": [
              {
                "type": "card",
                "constants": { "text": "Failures" },
                "nestedElements": [
                  {
                    "type": "listItem",
                    "expandWithProcessors": [{ "type": "path", "elements": ["failures"] }],
                    "processors": { "text": [{ "type": "path", "elements": ["title"] }] },
                    "emptyElement": { "type": "text", "constants": { "text": "No failures" } }
                  }
                ]
              }
            ] }
            """.trimIndent(),
            """{ "failures": [] }""",
        )

        assertEquals("No failures", assertNotNull(content).elements.single().nestedElements.single().text)
    }

    @Test
    fun anEmptyElementWithoutAnExpansionIsIgnored() {
        val content = parser.parseJSONToContent(
            """
            { "elements": [
              { "type": "text", "constants": { "text": "Always" }, "emptyElement": { "type": "text", "text": "Never" } }
            ] }
            """.trimIndent(),
        )

        assertEquals(listOf("Always"), assertNotNull(content).elements.map { it.text })
    }

    @Test
    fun everyCopyOfAnExpandedElementKeepsItsAction() {
        val content = KodicesParser().parseJSONToContent(
            """
            { "elements": [
              {
                "type": "listItem",
                "expandWithProcessors": [{ "type": "path", "elements": ["alerts"] }],
                "processors": { "text": [{ "type": "path", "elements": ["title"] }] },
                "action": { "type": "message", "constants": { "text": "Tapped" } }
              }
            ] }
            """.trimIndent(),
            """{ "alerts": [{ "title": "Disk full" }, { "title": "Fan" }] }""",
        )

        val elements = assertNotNull(content).elements
        assertEquals(2, elements.size)
        assertTrue(elements.all { it.actions.size == 1 }, "an action on a repeated element used to be dropped")
    }
}

package com.iktwo.kodices

import com.iktwo.kodices.dataprocessors.JSONDrillerProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KodicesTest {
    private val kodicesParser = KodicesParser()

    @Test
    fun testParseJSONToContentWithArray() {
        assertNull(kodicesParser.parseJSONToContent("[]"))
    }

    @Test
    fun testParseJSONToContentWithEmptyJSONObject() {
        assertNotNull(kodicesParser.parseJSONToContent("{}"))
    }

    @Test
    fun testParseJSONToContentWithEmptyElementsArray() {
        assertNotNull(kodicesParser.parseJSONToContent("{\"elements\":[]}"))
    }

    @Test
    fun testParseJSONToContentWithSingleElement() {
        val content = kodicesParser.parseJSONToContent("{\"element\":{\"type\":\"sample\"}}")
        assertNotNull(content)

        assertEquals(1, content.elements.size)
    }

    @Test
    fun testParseJSONToContentSingleRowWithWithArrayData() {
        val content =
            kodicesParser.parseJSONToContent(
                "{\"element\":{\"type\":\"row\", \"processors\": {\"text\":{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[0, \"scientificName\"]}}}}",
                sampleArrayData,
            )
        assertNotNull(content)

        assertEquals(1, content.elements.size)
        assertEquals("Calypte anna", content.elements[0].text)
    }

    @Test
    fun testParseJSONToContentWithArrayDataAndSingleProcessor() {
        val content =
            kodicesParser.parseJSONToContent(
                "{\"element\":{\"type\":\"row\", \"expandWithProcessor\": {\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[]}, \"processors\": {\"text\":{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"scientificName\"]}}}}",
                sampleArrayData.toString(),
            )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONToContentWithArrayDataMultipleProcessors() {
        val content =
            kodicesParser.parseJSONToContent(
                "{\"element\":{\"type\":\"row\", \"expandWithProcessors\": [{\"type\":\"${JSONDrillerProcessor.TYPE}\"}],\"processors\": {\"text\":{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"scientificName\"]}}}}",
                sampleArrayData.toString(),
            )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONToContentWithElementsArrayAndArrayDataMultipleProcessors() {
        val content = kodicesParser.parseJSONToContent(
            sampleContent.toString(),
            sampleArrayData,
        )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONElementToContent() {
        val content = kodicesParser.parseJSONElementToContent(
            sampleContent,
            sampleArrayData,
        )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONElementToContentNoData() {
        val content = kodicesParser.parseJSONElementToContent(
            sampleContentThatNeedsNoData,
            null,
        )
        assertNotNull(content)

        assertEquals(1, content.elements.size)
        assertEquals("Test row", content.elements[0].text)
    }
}

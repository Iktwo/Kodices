package com.iktwo.kodices

import com.iktwo.kodices.dataprocessors.JSONDrillerProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KodicesTest {
    private val kodices = Kodices()

    @Test
    fun testParseJSONToContentWithArray() {
        assertNull(kodices.parseJSONToContent("[]"))
    }

    @Test
    fun testParseJSONToContentWithEmptyJSONObject() {
        assertNotNull(kodices.parseJSONToContent("{}"))
    }

    @Test
    fun testParseJSONToContentWithEmptyElementsArray() {
        assertNotNull(kodices.parseJSONToContent("{\"elements\":[]}"))
    }

    @Test
    fun testParseJSONToContentWithSingleElement() {
        val content = kodices.parseJSONToContent("{\"element\":{\"type\":\"sample\"}}")
        assertNotNull(content)

        assertEquals(1, content.elements.size)
    }

    @Test
    fun testParseJSONToContentSingleRowWithWithArrayData() {
        val content =
            kodices.parseJSONToContent(
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
            kodices.parseJSONToContent(
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
            kodices.parseJSONToContent(
                "{\"element\":{\"type\":\"row\", \"expandWithProcessors\": [{\"type\":\"${JSONDrillerProcessor.TYPE}\"}],\"processors\": {\"text\":{\"type\":\"${JSONDrillerProcessor.TYPE}\", \"elements\":[\"scientificName\"]}}}}",
                sampleArrayData.toString(),
            )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONToContentWithElementsArrayAndArrayDataMultipleProcessors() {
        val content = kodices.parseJSONToContent(
            sampleContent.toString(),
            sampleArrayData,
        )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONElementToContent() {
        val content = kodices.parseJSONElementToContent(
            sampleContent,
            sampleArrayData,
        )
        assertNotNull(content)

        assertEquals(3, content.elements.size)
        assertEquals("Melanerpes formicivorus", content.elements[1].text)
    }

    @Test
    fun testParseJSONElementToContentNoData() {
        val content = kodices.parseJSONElementToContent(
            sampleContentThatNeedsNoData,
            null,
        )
        assertNotNull(content)

        assertEquals(1, content.elements.size)
        assertEquals("Test row", content.elements[0].text)
    }
}

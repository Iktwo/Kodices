package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.content.Content
import com.iktwo.piktographs.ui.chartRange
import com.iktwo.piktographs.ui.fraction
import com.iktwo.piktographs.ui.parseColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
public class DashboardElementsTest {
    private fun parse(
        json: String,
        data: String = "",
    ): Content = assertNotNull(KodicesParser(actions = listOf(MessageAction.descriptor)).parseJSONToContent(json, data))

    @Test
    public fun textRendersItsText() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "text", "constants": { "text": "Good morning", "style": "headlineMedium", "align": "center" } }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            onNodeWithText("Good morning").assertIsDisplayed()
        }

    @Test
    public fun statShowsLabelValueUnitAndCaptionForNumbersAndStrings() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "stat", "constants": { "text": "Living room", "value": 21.5, "unit": "°C", "textSecondary": "Warmer", "trend": "up" } },
                  { "type": "stat", "constants": { "text": "Outside", "value": "7" } }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            onNodeWithText("Living room").assertIsDisplayed()
            onNodeWithText("21.5").assertIsDisplayed()
            onNodeWithText("°C").assertIsDisplayed()
            onNodeWithText("Warmer").assertIsDisplayed()
            onNodeWithText("7").assertIsDisplayed()
        }

    @Test
    public fun statValueCanComeFromData() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  {
                    "type": "stat",
                    "constants": { "text": "Power", "unit": "W" },
                    "processors": { "value": [{ "type": "path", "elements": ["power", "watts"] }] }
                  }
                ] }
                """.trimIndent(),
                data = """{ "power": { "watts": 412 } }""",
            )

            setContent { PageUI(content = content) }

            onNodeWithText("412").assertIsDisplayed()
        }

    @Test
    public fun listItemShowsItsPartsAndTriggersItsAction() =
        runComposeUiTest {
            var triggered = 0
            val content = parse(
                """
                { "elements": [
                  {
                    "type": "listItem",
                    "constants": { "text": "Stand-up", "textSecondary": "Kitchen table", "value": "09:30", "icon": "calendar" },
                    "action": { "type": "message", "constants": { "text": "Tapped" } }
                  }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content, actionPerformer = ActionPerformer { triggered++ }) }

            onNodeWithText("Kitchen table").assertIsDisplayed()
            onNodeWithText("09:30").assertIsDisplayed()
            onNodeWithText("Stand-up").performClick()

            assertEquals(1, triggered)
        }

    @Test
    public fun badgeRendersBothVariants() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "badge", "constants": { "text": "Online" } },
                  { "type": "badge", "constants": { "text": "3 new", "variant": "outlined" } }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            onNodeWithText("Online").assertIsDisplayed()
            onNodeWithText("3 new").assertIsDisplayed()
        }

    @Test
    public fun gridPlacesElementsSideBySideAndWrapsToTheNextRow() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  {
                    "type": "grid",
                    "constants": { "columns": 2 },
                    "nestedElements": [
                      { "type": "text", "constants": { "text": "One" } },
                      { "type": "text", "constants": { "text": "Two" } },
                      { "type": "text", "constants": { "text": "Three" } }
                    ]
                  }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            val one = onNodeWithText("One").getBoundsInRoot()
            val two = onNodeWithText("Two").getBoundsInRoot()
            val three = onNodeWithText("Three").getBoundsInRoot()

            assertEquals(one.top, two.top, "the first two cells share a row")
            assertTrue(two.left > one.right - 1.dp, "the second cell is to the right of the first")
            assertTrue(three.top >= one.bottom, "the third cell wraps to a new row")
            assertEquals(one.left, three.left, "an incomplete last row keeps the column width")
        }

    @Test
    public fun chartDescribesItsValuesForBothVariantsAndSurvivesBadInput() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "chart", "constants": { "text": "Temperature", "values": [18.5, 17, "21"], "labels": ["then", "now"] } },
                  { "type": "chart", "constants": { "text": "Steps", "variant": "bar", "values": [4200, 0, -5] } },
                  { "type": "chart", "constants": { "text": "Empty", "values": "not an array" } },
                  { "type": "chart", "constants": { "text": "Flat", "values": [3, 3, 3] } }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            onNodeWithContentDescription("Temperature: 18.5, 17.0, 21.0").assertIsDisplayed()
            onNodeWithContentDescription("Steps: 4200.0, 0.0, -5.0").assertIsDisplayed()
            onNodeWithText("now").assertIsDisplayed()
            onNodeWithText("Empty").assertIsDisplayed()
            onNodeWithText("Flat").assertIsDisplayed()
        }

    @Test
    public fun chartValuesCanComeFromData() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  {
                    "type": "chart",
                    "constants": { "text": "Load" },
                    "processors": { "values": [{ "type": "path", "elements": ["load"] }] }
                  }
                ] }
                """.trimIndent(),
                data = """{ "load": [1, 2, 4] }""",
            )

            setContent { PageUI(content = content) }

            onNodeWithContentDescription("Load: 1.0, 2.0, 4.0").assertIsDisplayed()
        }

    @Test
    public fun chartRangeStartsBarsAtZeroAndKeepsFlatSeriesVisible() {
        assertEquals(0f..10f, chartRange(listOf(4f, 10f), isBar = true, min = null, max = null))
        assertEquals(-5f..10f, chartRange(listOf(-5f, 10f), isBar = true, min = null, max = null))
        assertEquals(4f..10f, chartRange(listOf(4f, 10f), isBar = false, min = null, max = null))
        assertEquals(2f..4f, chartRange(listOf(3f, 3f), isBar = false, min = null, max = null))
        assertEquals(0f..1f, chartRange(emptyList(), isBar = true, min = null, max = null))
        assertEquals(0f..100f, chartRange(listOf(4f, 10f), isBar = false, min = 0f, max = 100f))

        assertEquals(0.5f, (0f..10f).fraction(5f))
        assertEquals(1f, (0f..10f).fraction(50f), "values outside the range are clamped")
    }

    @Test
    public fun parseColorAcceptsRgbAndArgbHexOnly() {
        assertEquals(0xFFFF0000.toInt(), parseColor("#FF0000")?.let { (it.value shr 32).toInt() })
        assertEquals(0x80FF0000.toInt(), parseColor("#80FF0000")?.let { (it.value shr 32).toInt() })
        assertNull(parseColor("red"))
        assertNull(parseColor("#12"))
        assertNull(parseColor("#GGGGGG"))
        assertNull(parseColor(null))
    }
}

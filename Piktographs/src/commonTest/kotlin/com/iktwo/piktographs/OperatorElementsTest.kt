package com.iktwo.piktographs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.content.Content
import com.iktwo.piktographs.navigation.NavigateAction
import com.iktwo.piktographs.ui.CellDisplay
import com.iktwo.piktographs.ui.Tone
import com.iktwo.piktographs.ui.adaptiveColumns
import com.iktwo.piktographs.ui.cellValue
import com.iktwo.piktographs.ui.contentColorOn
import com.iktwo.piktographs.ui.parseFontFamily
import com.iktwo.piktographs.ui.parseTableColumns
import com.iktwo.piktographs.ui.parseTone
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tone, tables, tabs, adaptive grids and empty states: what a dense, data-driven screen needs. */
@OptIn(ExperimentalTestApi::class)
public class OperatorElementsTest {
    private val parser = KodicesParser(actions = listOf(NavigateAction.descriptor))

    private fun parse(
        json: String,
        data: String = "",
    ): Content = assertNotNull(parser.parseJSONToContent(json, data))

    private val columns = """
        [
          { "key": "name", "label": "Device", "weight": 2, "secondaryKey": "id" },
          { "key": "status", "label": "Status", "width": 90, "display": "badge", "toneKey": "statusTone" },
          { "key": "sent", "label": "Sent", "width": 60, "align": "end" }
        ]
    """.trimIndent()

    private val table = """
        { "elements": [
          { "type": "tableHeader", "constants": { "columns": $columns } },
          {
            "type": "tableRow",
            "constants": { "columns": $columns },
            "expandWithProcessors": [{ "type": "path", "elements": ["devices"] }],
            "processors": {
              "values": { "type": "path" },
              "tone": [{ "type": "path", "elements": ["severity"] }],
              "statusTone": [
                { "type": "path", "elements": ["status"] },
                { "type": "map", "values": { "Online": "success" }, "default": "neutral" }
              ]
            },
            "action": { "type": "navigate", "constants": { "targetRoute": "device" } },
            "emptyElement": { "type": "text", "constants": { "text": "No devices" } }
          }
        ] }
    """.trimIndent()

    private val devices = """
        { "devices": [
          { "name": "Desk", "id": "77aa01bc", "status": "Online", "sent": 40, "severity": "info" },
          { "name": "Tablet", "id": "0b9e44d1", "status": "Offline", "sent": 0, "severity": "warning" }
        ] }
    """.trimIndent()

    @Test
    public fun toneNamesIncludeTheOnesServersUse() {
        assertEquals(Tone.Warning, parseTone("warning"))
        assertEquals(Tone.Warning, parseTone(" WARN "))
        assertEquals(Tone.Error, parseTone("ERROR"))
        assertEquals(Tone.Error, parseTone("critical"))
        assertEquals(Tone.Success, parseTone("ok"))
        assertEquals(Tone.Info, parseTone("info"))
        assertEquals(Tone.Neutral, parseTone("neutral"))
        assertEquals(Tone.Neutral, parseTone("purple"))
        assertEquals(Tone.Neutral, parseTone(null))
        assertTrue(Tone.Error.needsAttention && Tone.Warning.needsAttention)
        assertTrue(!Tone.Info.needsAttention && !Tone.Success.needsAttention && !Tone.Neutral.needsAttention)
    }

    @Test
    public fun textOnAToneIsBlackOrWhiteWhicheverReads() {
        assertEquals(Color.White, contentColorOn(Color(0xFF1B7F3B)))
        assertEquals(Color.Black, contentColorOn(Color(0xFFFFB95C)))
    }

    @Test
    public fun fontFamiliesAreNamed() {
        assertEquals(FontFamily.Monospace, parseFontFamily("monospace"))
        assertEquals(FontFamily.Monospace, parseFontFamily("Mono"))
        assertEquals(FontFamily.Serif, parseFontFamily("serif"))
        assertEquals(FontFamily.SansSerif, parseFontFamily("sans-serif"))
        assertNull(parseFontFamily("comic"))
        assertNull(parseFontFamily(null))
    }

    @Test
    public fun anAdaptiveGridFitsAsManyColumnsAsAreWideEnough() {
        assertEquals(1, adaptiveColumns(360.dp, 420.dp, 4.dp, 12))
        assertEquals(2, adaptiveColumns(360.dp, 150.dp, 4.dp, 12))
        // Two 420 dp columns and the gap between them need 844 dp.
        assertEquals(1, adaptiveColumns(843.dp, 420.dp, 4.dp, 12))
        assertEquals(2, adaptiveColumns(844.dp, 420.dp, 4.dp, 12))
        assertEquals(3, adaptiveColumns(1280.dp, 150.dp, 0.dp, 3), "columns is the most it uses")
    }

    @Test
    public fun anAdaptiveGridStacksInANarrowWidthAndSitsSideBySideInAWideOne() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "grid", "constants": { "minColumnWidth": 200 }, "nestedElements": [
                    { "type": "text", "constants": { "text": "Left" } },
                    { "type": "text", "constants": { "text": "Right" } }
                  ] }
                ] }
                """.trimIndent(),
            )
            var width by mutableStateOf(300.dp)

            setContent { Box(Modifier.width(width)) { PageUI(content = content) } }
            val stackedLeft = onNodeWithText("Left").getBoundsInRoot()
            val stackedRight = onNodeWithText("Right").getBoundsInRoot()
            assertTrue(stackedRight.top >= stackedLeft.bottom, "one column: Right is under Left")
            assertEquals(stackedLeft.left, stackedRight.left)

            width = 600.dp
            waitForIdle()
            val left = onNodeWithText("Left").getBoundsInRoot()
            val right = onNodeWithText("Right").getBoundsInRoot()
            assertEquals(left.top, right.top, "two columns: on one line")
            assertTrue(right.left >= left.right)
        }

    @Test
    public fun columnsAreReadFromTheirJsonAndBadOnesSkipped() {
        val parsed = parseTableColumns(
            Json.parseToJsonElement(
                """
                [
                  { "key": "time", "label": "Time", "width": 96, "display": "mono", "secondaryKey": "date" },
                  { "key": "level", "display": "badge", "toneKey": "levelTone", "align": "end", "maxLines": 1 },
                  { "key": "icon", "display": "icon" },
                  { "key": "message", "weight": 3, "primary": true },
                  { "label": "No key" },
                  "not an object"
                ]
                """.trimIndent(),
            ),
        )

        assertEquals(listOf("time", "level", "icon", "message"), parsed.map { it.key })
        assertEquals(96.dp, parsed[0].width)
        assertEquals(CellDisplay.Mono, parsed[0].display)
        assertEquals("date", parsed[0].secondaryKey)
        assertEquals(CellDisplay.Badge, parsed[1].display)
        assertEquals("levelTone", parsed[1].toneKey)
        assertEquals(TextAlign.End, parsed[1].align)
        assertEquals(1, parsed[1].maxLines)
        assertEquals(CellDisplay.Icon, parsed[2].display)
        assertEquals(3f, parsed[3].weight)
        assertTrue(parsed[3].primary)
        assertNull(parsed[3].width)
        assertEquals(2, parsed[3].maxLines)
        assertEquals(CellDisplay.Text, parsed[3].display)

        assertTrue(parseTableColumns(null).isEmpty())
        assertTrue(parseTableColumns(Json.parseToJsonElement("\"columns\"")).isEmpty())
    }

    @Test
    public fun aRowReadsItsOwnKeysBeforeItsValuesObject() {
        val row = parse(
            """
            { "elements": [
              {
                "type": "tableRow",
                "constants": { "columns": [{ "key": "name" }], "status": "Own" },
                "processors": { "values": { "type": "path" } }
              }
            ] }
            """.trimIndent(),
            """{ "name": "Desk", "status": "From values", "sent": 40, "gone": null }""",
        ).elements.single()

        assertEquals("Desk", row.cellValue("name"))
        assertEquals("Own", row.cellValue("status"))
        assertEquals("40", row.cellValue("sent"), "numbers are shown as written")
        assertNull(row.cellValue("gone"))
        assertNull(row.cellValue("missing"))
    }

    @Test
    public fun aWideTableShowsOneHeaderAndARowPerEntry() =
        runComposeUiTest {
            val content = parse(table, devices)

            setContent { Box(Modifier.width(800.dp)) { PageUI(content = content) } }

            onNodeWithText("Device").assertIsDisplayed()
            onNodeWithText("Status").assertIsDisplayed()
            onNodeWithText("Desk").assertIsDisplayed()
            onNodeWithText("77aa01bc").assertIsDisplayed()
            onNodeWithText("Online").assertIsDisplayed()
            onNodeWithText("Tablet").assertIsDisplayed()
            onNodeWithText("Offline").assertIsDisplayed()

            // Cells line up under their titles.
            assertEquals(
                onNodeWithText("Device").getBoundsInRoot().left,
                // Unmerged: a clickable row is one node to the accessibility tree.
                onNodeWithText("Desk", useUnmergedTree = true).getBoundsInRoot().left,
            )
            assertEquals(
                onNodeWithText("Sent").getBoundsInRoot().right,
                onNodeWithText("40", useUnmergedTree = true).getBoundsInRoot().right,
                "an end-aligned column ends where its title does",
            )
        }

    @Test
    public fun aNarrowTableDropsItsHeaderAndLabelsEachValueInTheRow() =
        runComposeUiTest {
            val content = parse(table, devices)

            setContent { Box(Modifier.width(360.dp)) { PageUI(content = content) } }

            onAllNodesWithText("Device").assertCountEquals(0)
            onNodeWithText("Desk").assertIsDisplayed()
            onNodeWithText("77aa01bc").assertIsDisplayed()
            onNodeWithText("Online").assertIsDisplayed()
            // One "Sent" per row, in front of its value, instead of one above the column.
            onAllNodesWithText("Sent").assertCountEquals(2)
            onNodeWithText("40").assertIsDisplayed()
        }

    @Test
    public fun aTableThatNeverCollapsesKeepsItsHeader() =
        runComposeUiTest {
            val content = parse(table.replace("\"columns\": $columns }", "\"columns\": $columns, \"collapseBelow\": 0 }"), devices)

            setContent { Box(Modifier.width(360.dp)) { PageUI(content = content) } }

            onNodeWithText("Device").assertIsDisplayed()
            onAllNodesWithText("Sent").assertCountEquals(1)
        }

    @Test
    public fun aRowRunsItsAction() =
        runComposeUiTest {
            val performed = mutableListOf<Action>()
            val content = parse(table, devices)

            setContent {
                Box(Modifier.width(800.dp)) {
                    PageUI(content = content, actionPerformer = ActionPerformer { performed += it })
                }
            }

            onNodeWithText("Tablet").performClick()
            assertEquals("device", assertIs<NavigateAction>(performed.single()).targetRoute)
        }

    @Test
    public fun anEmptyExpansionRendersItsEmptyElement() =
        runComposeUiTest {
            val content = parse(table, """{ "devices": [] }""")

            setContent { Box(Modifier.width(800.dp)) { PageUI(content = content) } }

            onNodeWithText("No devices").assertIsDisplayed()
        }

    @Test
    public fun topLevelTabsStayAboveThePageAndRunTheirActions() =
        runComposeUiTest {
            val performed = mutableListOf<Action>()
            val content = parse(
                """
                { "elements": [
                  { "type": "text", "constants": { "text": "First line of the page" } },
                  { "type": "tabs", "nestedElements": [
                    { "type": "tab", "constants": { "text": "Overview", "icon": "home", "selected": true },
                      "action": { "type": "navigate", "constants": { "targetRoute": "overview" } } },
                    { "type": "tab", "constants": { "text": "Logs", "icon": "list" },
                      "action": { "type": "navigate", "constants": { "targetRoute": "logs" } } }
                  ] }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content, actionPerformer = ActionPerformer { performed += it }) }

            onNodeWithText("Overview").assertIsSelected()
            onNodeWithText("Logs").assertIsNotSelected()
            assertTrue(
                onNodeWithText("Overview").getBoundsInRoot().bottom <= onNodeWithText("First line of the page").getBoundsInRoot().top,
                "the tabs are above the page even though the JSON lists them after its first element",
            )
            onAllNodesWithText("Overview").assertCountEquals(1)

            onNodeWithText("Logs").performClick()
            assertEquals("logs", assertIs<NavigateAction>(performed.single()).targetRoute)
        }

    @Test
    public fun aBlankBadgeDrawsNothingAndAListItemShowsItsBadge() =
        runComposeUiTest {
            val content = parse(
                """
                { "elements": [
                  { "type": "badge", "constants": { "text": "", "tone": "warning" } },
                  { "type": "badge", "constants": { "text": "Unproven", "tone": "warning", "variant": "tonal" } },
                  { "type": "listItem", "constants": { "text": "Pixel 9", "badge": "New", "tone": "info", "dense": true, "variant": "tonal" } },
                  { "type": "listItem", "constants": { "text": "Desk", "badge": "" } },
                  { "type": "text", "constants": { "text": "log line", "fontFamily": "monospace", "tone": "error" } }
                ] }
                """.trimIndent(),
            )

            setContent { PageUI(content = content) }

            onNodeWithText("Unproven").assertIsDisplayed()
            onNodeWithText("New").assertIsDisplayed()
            onNodeWithText("Pixel 9").assertIsDisplayed()
            onNodeWithText("Desk").assertIsDisplayed()
            onNodeWithText("log line").assertIsDisplayed()
        }

    @Test
    public fun aStatWithAnActionIsClickableAndTakesATone() =
        runComposeUiTest {
            val performed = mutableListOf<Action>()
            val content = parse(
                """
                { "elements": [
                  {
                    "type": "stat",
                    "constants": { "text": "Push failures", "size": "small" },
                    "processors": {
                      "value": [{ "type": "path", "elements": ["failures"] }],
                      "tone": [
                        { "type": "path", "elements": ["failures"] },
                        { "type": "map", "values": { "0": "neutral" }, "default": "error" }
                      ]
                    },
                    "action": { "type": "navigate", "constants": { "targetRoute": "deliveries" } }
                  }
                ] }
                """.trimIndent(),
                """{ "failures": 2 }""",
            )

            setContent { PageUI(content = content, actionPerformer = ActionPerformer { performed += it }) }

            onNodeWithText("2").assertIsDisplayed()
            onNodeWithText("Push failures").performClick()
            assertEquals("deliveries", assertIs<NavigateAction>(performed.single()).targetRoute)
        }
}

package com.iktwo.piktographs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Logger
import com.iktwo.piktographs.ui.containerDeprecations
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
public class ContainerAliasesTest {
    private val children = listOf(
        ProcessedElement(type = "text", id = "first", text = "First"),
        ProcessedElement(type = "text", id = "second", text = "Second"),
    )

    private fun container(
        type: String = "container",
        vararg values: Pair<String, String>,
    ) = ProcessedElement(
        type = type,
        id = "box",
        nestedElements = children,
        jsonValues = values.associate { (key, value) -> key to JsonPrimitive(value) },
    )

    private class RecordingLogger : Logger {
        val warnings = mutableListOf<String>()

        override fun debug(message: String) = Unit

        override fun info(message: String) = Unit

        override fun warn(message: String) {
            warnings += message
        }

        override fun error(message: String) = Unit
    }

    @Test
    public fun documentedSpellingHasNothingToReport() {
        assertEquals(emptyList(), containerDeprecations(container()))
        assertEquals(
            emptyList(),
            containerDeprecations(
                container(
                    "container",
                    "orientation" to "vertical",
                    "arrangement" to "space_between",
                    "alignment" to "start",
                    "paddingHorizontal" to "8",
                ),
            ),
        )
    }

    @Test
    public fun deprecatedTypeNamesSayWhatToWriteInstead() {
        assertEquals(
            listOf("""Element "box": type "hrow" is deprecated, use "type": "container"."""),
            containerDeprecations(container("hrow")),
        )
        assertEquals(
            listOf("""Element "box": type "vrow" is deprecated, use "type": "container" with "orientation": "vertical"."""),
            containerDeprecations(container("vrow")),
        )
        assertEquals(1, containerDeprecations(container("horizontal_layout")).size)
        assertEquals(1, containerDeprecations(container("vertical_layout")).size)
    }

    @Test
    public fun deprecatedKeysAndValuesAreEachReported() {
        val messages = containerDeprecations(
            container(
                "container",
                "direction" to "column",
                "horizontalPadding" to "8",
                "verticalPadding" to "8",
                "arrangement" to "spaceBetween",
                "alignment" to "left",
            ),
        )

        assertEquals(
            listOf(
                """Element "box": key "direction" is deprecated, use "orientation".""",
                """Element "box": key "horizontalPadding" is deprecated, use "paddingHorizontal".""",
                """Element "box": key "verticalPadding" is deprecated, use "paddingVertical".""",
                """Element "box": "orientation": "column" is deprecated, use "vertical".""",
                """Element "box": "arrangement": "spaceBetween" is deprecated, use "space_between".""",
                """Element "box": "alignment": "left" is deprecated, use "start".""",
            ),
            messages,
        )
    }

    @Test
    public fun verticalContainerStacksItsChildren() =
        runComposeUiTest {
            setContent {
                PageUI(Content(listOf(container("container", "orientation" to "vertical"))))
            }

            val first = onNodeWithText("First").getBoundsInRoot()
            val second = onNodeWithText("Second").getBoundsInRoot()
            assertTrue(second.top >= first.bottom, "expected a column: $first then $second")
        }

    @Test
    public fun containerIsARowByDefault() =
        runComposeUiTest {
            setContent { PageUI(Content(listOf(container()))) }

            val first = onNodeWithText("First").getBoundsInRoot()
            val second = onNodeWithText("Second").getBoundsInRoot()
            assertTrue(second.left >= first.right, "expected a row: $first then $second")
        }

    @Test
    public fun deprecatedNameStillRendersAndWarnsOnce() =
        runComposeUiTest {
            val logger = RecordingLogger()

            setContent {
                CompositionLocalProvider(LocalLogger provides logger) {
                    PageUI(Content(listOf(container("vrow"))))
                }
            }

            onNodeWithText("First").assertIsDisplayed()
            val first = onNodeWithText("First").getBoundsInRoot()
            val second = onNodeWithText("Second").getBoundsInRoot()
            assertTrue(second.top >= first.bottom, "vrow is still a column")

            waitForIdle()
            assertEquals(1, logger.warnings.size, logger.warnings.toString())
        }
}

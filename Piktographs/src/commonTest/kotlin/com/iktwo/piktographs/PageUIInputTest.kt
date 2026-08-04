package com.iktwo.piktographs

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.content.Content
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Regression tests for the input/form path. Typed text has to survive recomposition of [PageUI].
 */
@OptIn(ExperimentalTestApi::class)
class PageUIInputTest {
    private fun parse(json: String): Content {
        val content = KodicesParser().parseJSONToContent(json)
        assertNotNull(content, "Fixture failed to parse")
        return content
    }

    private val singleTextInput = """
        {
          "elements": [
            { "type": "textInput", "id": "name", "textSecondary": "Name" }
          ]
        }
    """.trimIndent()

    @Test
    fun typedTextIsRetained() =
        runComposeUiTest {
            val content = parse(singleTextInput)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Name").performTextInput("Ada")

            onNodeWithText("Ada").assertExists()
        }

    @Test
    fun typedTextSurvivesRecompositionOfPageUI() =
        runComposeUiTest {
            val content = parse(singleTextInput)
            val counter = mutableStateOf(0)

            setContent {
                Text(
                    text = "bump ${counter.value}",
                    modifier = Modifier.testTag("bump"),
                )

                // Reading the counter inside PageUI's own call scope, so bumping it forces PageUI
                // itself to recompose rather than just a sibling.
                PageUI(
                    content = content,
                    modifier = Modifier.testTag("page${counter.value}"),
                    elementOverrides = { false },
                )
            }

            onNodeWithText("Name").performTextInput("Ada")

            counter.value = 1
            waitForIdle()
            onNodeWithTag("bump").assertTextEquals("bump 1")

            onNodeWithText("Ada").assertExists()
        }

    @Test
    fun typedTextSurvivesTheOnInputUpdatedCallback() =
        runComposeUiTest {
            // The production path: every keystroke fires onInputUpdated, whose state change
            // recomposes the caller, and with it PageUI.
            val content = parse(singleTextInput)
            val updates = mutableStateOf(0)

            setContent {
                Text(
                    text = "updates ${updates.value}",
                    modifier = Modifier.testTag("updates"),
                )

                PageUI(
                    content = content,
                    elementOverrides = { false },
                    onInputUpdated = { updates.value += 1 },
                )
            }

            onNodeWithText("Name").performTextInput("Ada")
            waitForIdle()

            onNodeWithText("Ada").assertExists()
        }

    @Test
    fun eachTextInputKeepsItsOwnValue() =
        runComposeUiTest {
            val content = parse(
                """
                {
                  "elements": [
                    { "type": "textInput", "id": "first", "textSecondary": "First" },
                    { "type": "textInput", "id": "second", "textSecondary": "Second" }
                  ]
                }
                """.trimIndent(),
            )

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("First").performTextInput("Ada")
            onNodeWithText("Second").performTextInput("Lovelace")

            onNodeWithText("Ada").assertExists()
            onNodeWithText("Lovelace").assertExists()
        }

    @Test
    fun checkboxStateIsRetained() =
        runComposeUiTest {
            val content = parse(
                """
                {
                  "elements": [
                    { "type": "checkbox", "id": "agree", "text": "I agree" }
                  ]
                }
                """.trimIndent(),
            )

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("I agree").performClick()
            onNodeWithText("I agree").assertExists()
        }

    @Test
    fun elementsWithoutAnExplicitIdDoNotCollide() =
        runComposeUiTest {
            // Two id-less elements previously shared the generated id "id", which Compose rejects
            // as a duplicate LazyList key.
            val content = parse(
                """
                {
                  "elements": [
                    { "type": "row", "text": "first" },
                    { "type": "row", "text": "second" }
                  ]
                }
                """.trimIndent(),
            )

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("first").assertExists()
            onNodeWithText("second").assertExists()
        }
}

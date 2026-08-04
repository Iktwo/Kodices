package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.MessageAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
public class Phase1ComponentsTest {
    @Test
    public fun rendersButtonAndTriggersActionOnClick() =
        runComposeUiTest {
            var actionTriggeredCount = 0
            val parser = KodicesParser(actions = listOf(MessageAction.descriptor))
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "button",
                      "id": "btn1",
                      "text": "Click Me",
                      "action": { "type": "message", "constants": { "text": "Clicked!" } }
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            val actionPerformer = ActionPerformer {
                actionTriggeredCount += 1
            }

            setContent {
                PageUI(
                    content = content,
                    elementOverrides = { false },
                    actionPerformer = actionPerformer,
                )
            }

            onNodeWithText("Click Me").assertIsDisplayed()
            onNodeWithText("Click Me").performClick()

            assertEquals(1, actionTriggeredCount)
        }

    @Test
    public fun rendersCardWithNestedContent() =
        runComposeUiTest {
            val content = KodicesParser().parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "card",
                      "id": "card1",
                      "text": "Card Header",
                      "textSecondary": "Card Subtitle",
                      "nestedElements": [
                        { "type": "row", "id": "inner_row", "text": "Inner Row Text" }
                      ]
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Card Header").assertIsDisplayed()
            onNodeWithText("Card Subtitle").assertIsDisplayed()
            onNodeWithText("Inner Row Text").assertIsDisplayed()
        }

    @Test
    public fun rendersProgressIndicatorWithLabels() =
        runComposeUiTest {
            val content = KodicesParser().parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "progress",
                      "id": "prog1",
                      "text": "Downloading Update",
                      "textSecondary": "50%",
                      "variant": "linear",
                      "progress": 0.5
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Downloading Update").assertIsDisplayed()
            onNodeWithText("50%").assertIsDisplayed()
        }
}

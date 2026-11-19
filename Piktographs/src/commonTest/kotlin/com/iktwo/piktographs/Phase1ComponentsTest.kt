package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
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

    @Test
    public fun rendersSeparatorElement() =
        runComposeUiTest {
            val content = KodicesParser().parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "row1", "text": "Above Separator" },
                    { "type": "separator", "id": "sep1", "style": "line" },
                    { "type": "row", "id": "row2", "text": "Below Separator" }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Above Separator").assertIsDisplayed()
            onNodeWithText("Below Separator").assertIsDisplayed()
        }

    @Test
    public fun rendersSwitchElementAndToggles() =
        runComposeUiTest {
            val parser = KodicesParser()
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "switch", "id": "dark_mode", "text": "Dark Mode", "textSecondary": "Enable dark theme" }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Dark Mode").assertIsDisplayed()
            onNodeWithText("Enable dark theme").assertIsDisplayed()
            onNodeWithText("Dark Mode").performClick()
        }

    @Test
    public fun rendersIconElementAndHandlesClick() =
        runComposeUiTest {
            var iconClicked = false
            val parser = KodicesParser(actions = listOf(MessageAction.descriptor))
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "icon",
                      "id": "settings_icon",
                      "name": "settings",
                      "text": "Settings Icon",
                      "action": { "type": "message", "constants": { "text": "Settings clicked" } }
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(
                    content = content,
                    elementOverrides = { false },
                    actionPerformer = ActionPerformer { iconClicked = true },
                )
            }

            onNodeWithContentDescription("Settings Icon").assertIsDisplayed()
            onNodeWithContentDescription("Settings Icon").performClick()

            assertEquals(true, iconClicked)
        }

    @Test
    public fun rendersImageElementWithConfiguredAttributes() =
        runComposeUiTest {
            val parser = KodicesParser()
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "image",
                      "id": "hero_img",
                      "url": "https://example.com/image.png",
                      "contentDescription": "Hero Image",
                      "width": 200,
                      "height": 100,
                      "contentScale": "crop",
                      "cornerRadius": 8
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithContentDescription("Hero Image").assertIsDisplayed()
        }

    @Test
    public fun rendersHorizontalContainerWithWeightedChildren() =
        runComposeUiTest {
            val parser = KodicesParser(actions = listOf(MessageAction.descriptor))
            var button1Clicked = false
            var button2Clicked = false
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "container",
                      "id": "action_row",
                      "spacing": 16,
                      "nestedElements": [
                        {
                          "type": "button",
                          "id": "btn_cancel",
                          "text": "Cancel",
                          "weight": 1.0,
                          "action": { "type": "message", "constants": { "text": "cancel" } }
                        },
                        {
                          "type": "button",
                          "id": "btn_confirm",
                          "text": "Confirm",
                          "weight": 2.0,
                          "action": { "type": "message", "constants": { "text": "confirm" } }
                        }
                      ]
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(
                    content = content,
                    elementOverrides = { false },
                    actionPerformer = ActionPerformer { action ->
                        val msg = (action as? MessageAction)?.text
                        if (msg == "cancel") button1Clicked = true
                        if (msg == "confirm") button2Clicked = true
                    },
                )
            }

            onNodeWithText("Cancel").assertIsDisplayed()
            onNodeWithText("Confirm").assertIsDisplayed()

            onNodeWithText("Cancel").performClick()
            assertEquals(true, button1Clicked)

            onNodeWithText("Confirm").performClick()
            assertEquals(true, button2Clicked)
        }

    @Test
    public fun rendersVerticalContainerAndClickableContainer() =
        runComposeUiTest {
            var containerClicked = false
            val parser = KodicesParser(actions = listOf(MessageAction.descriptor))
            val content = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "container",
                      "id": "clickable_container",
                      "orientation": "vertical",
                      "padding": 12,
                      "spacing": 8,
                      "action": { "type": "message", "constants": { "text": "container_clicked" } },
                      "nestedElements": [
                        { "type": "row", "id": "inner_label", "text": "Item in Container" }
                      ]
                    }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(
                    content = content,
                    elementOverrides = { false },
                    actionPerformer = ActionPerformer { containerClicked = true },
                )
            }

            onNodeWithText("Item in Container").assertIsDisplayed()
            onNodeWithText("Item in Container").performClick()

            assertEquals(true, containerClicked)
        }
}

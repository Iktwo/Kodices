package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.content.Content
import com.iktwo.piktographs.navigation.KodicesNavHost
import com.iktwo.piktographs.navigation.NavigateAction
import com.iktwo.piktographs.navigation.rememberKodicesNavController
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
public class KodicesNavHostTest {
    @Test
    public fun loadsInitialRouteAndNavigatesToNextPage() =
        runComposeUiTest {
            val parser = KodicesParser(actions = listOf(NavigateAction.descriptor, MessageAction.descriptor))

            val homeContent = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "row",
                      "id": "home_title",
                      "text": "Home Screen"
                    },
                    {
                      "type": "button",
                      "id": "nav_btn",
                      "text": "Go to Detail",
                      "action": { "type": "navigate", "targetRoute": "detail" }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val detailContent = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "row",
                      "id": "detail_title",
                      "text": "Detail Screen"
                    },
                    {
                      "type": "button",
                      "id": "back_btn",
                      "text": "Go Back",
                      "action": { "type": "back" }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val fetcher: suspend (String) -> Content? = { route ->
                when (route) {
                    "home" -> homeContent
                    "detail" -> detailContent
                    else -> null
                }
            }

            setContent {
                val navController = rememberKodicesNavController(initialRoute = "home")
                KodicesNavHost(
                    navController = navController,
                    fetchContent = fetcher,
                )
            }

            // Assert Home Screen is displayed
            onNodeWithText("Home Screen").assertIsDisplayed()
            onNodeWithText("Go to Detail").assertIsDisplayed()

            // Click Go to Detail button
            onNodeWithText("Go to Detail").performClick()

            // Assert Detail Screen is displayed
            onNodeWithText("Detail Screen").assertIsDisplayed()
            onNodeWithText("Go Back").assertIsDisplayed()

            // Click Go Back button
            onNodeWithText("Go Back").performClick()

            // Assert Home Screen is back
            onNodeWithText("Home Screen").assertIsDisplayed()
        }

    @Test
    public fun displaysErrorContentWhenRouteFails() =
        runComposeUiTest {
            var fetchCount = 0
            val fetcher: suspend (String) -> Content? = {
                fetchCount += 1
                if (fetchCount > 1) {
                    KodicesParser().parseJSONToContent(
                        """
                        {
                          "elements": [
                            { "type": "row", "id": "r1", "text": "Recovered" }
                          ]
                        }
                        """.trimIndent(),
                    )
                } else {
                    null
                }
            }

            setContent {
                val navController = rememberKodicesNavController(initialRoute = "unknown_route")
                KodicesNavHost(
                    navController = navController,
                    fetchContent = fetcher,
                )
            }

            // Assert error message and retry button
            onNodeWithText("Failed to load route: unknown_route").assertIsDisplayed()
            onNodeWithText("Retry").assertIsDisplayed()

            // Click retry
            onNodeWithText("Retry").performClick()

            // Assert recovered content is displayed
            onNodeWithText("Recovered").assertIsDisplayed()
            assertEquals(2, fetchCount)
        }

    @Test
    public fun navigatesUsingUnregisteredGenericAction() =
        runComposeUiTest {
            // Bare parser without registering NavigateAction.descriptor
            val parser = KodicesParser()

            val page1 = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "p1_title", "text": "Page One" },
                    {
                      "type": "button",
                      "id": "nav_btn",
                      "text": "Go to Page 2",
                      "action": {
                        "type": "navigate",
                        "constants": {
                          "targetRoute": "page_two"
                        }
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val page2 = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "p2_title", "text": "Page Two" }
                  ]
                }
                """.trimIndent(),
            )

            val fetcher: suspend (String) -> Content? = { route ->
                when (route) {
                    "page_one" -> page1
                    "page_two" -> page2
                    else -> null
                }
            }

            setContent {
                val navController = rememberKodicesNavController(initialRoute = "page_one")
                KodicesNavHost(
                    navController = navController,
                    fetchContent = fetcher,
                )
            }

            onNodeWithText("Page One").assertIsDisplayed()
            onNodeWithText("Go to Page 2").performClick()
            onNodeWithText("Page Two").assertIsDisplayed()
        }

    @Test
    public fun navigatesToModalDialogAndDismisses() =
        runComposeUiTest {
            val parser = KodicesParser(actions = listOf(NavigateAction.descriptor, MessageAction.descriptor))

            val mainPage = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "main_title", "text": "Main Page Content" },
                    {
                      "type": "button",
                      "id": "open_dialog_btn",
                      "text": "Open Dialog",
                      "action": {
                        "type": "navigate",
                        "targetRoute": "my_dialog",
                        "presentation": "dialog",
                        "title": "Info Dialog"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val dialogPage = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "dialog_text", "text": "Inside Modal Dialog" },
                    {
                      "type": "button",
                      "id": "close_dialog_btn",
                      "text": "Close Dialog",
                      "action": { "type": "dismiss_modal" }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val fetcher: suspend (String) -> Content? = { route ->
                when (route) {
                    "main" -> mainPage
                    "my_dialog" -> dialogPage
                    else -> null
                }
            }

            setContent {
                val navController = rememberKodicesNavController(initialRoute = "main")
                KodicesNavHost(
                    navController = navController,
                    fetchContent = fetcher,
                )
            }

            onNodeWithText("Main Page Content").assertIsDisplayed()
            onNodeWithText("Open Dialog").performClick()

            onNodeWithText("Inside Modal Dialog").assertIsDisplayed()
            onNodeWithText("Close Dialog").performClick()

            onNodeWithText("Main Page Content").assertIsDisplayed()
        }

    @Test
    public fun navigatesToBottomSheetAndDismisses() =
        runComposeUiTest {
            val parser = KodicesParser(actions = listOf(NavigateAction.descriptor, MessageAction.descriptor))

            val mainPage = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "main_title", "text": "Home for Sheet" },
                    {
                      "type": "button",
                      "id": "open_sheet_btn",
                      "text": "Open Sheet",
                      "action": {
                        "type": "navigate",
                        "targetRoute": "my_sheet",
                        "presentation": "bottom_sheet"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val sheetPage = parser.parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "sheet_text", "text": "Inside Bottom Sheet" },
                    {
                      "type": "button",
                      "id": "dismiss_sheet_btn",
                      "text": "Dismiss Sheet",
                      "action": { "type": "back" }
                    }
                  ]
                }
                """.trimIndent(),
            )

            val fetcher: suspend (String) -> Content? = { route ->
                when (route) {
                    "home_sheet" -> mainPage
                    "my_sheet" -> sheetPage
                    else -> null
                }
            }

            setContent {
                val navController = rememberKodicesNavController(initialRoute = "home_sheet")
                KodicesNavHost(
                    navController = navController,
                    fetchContent = fetcher,
                )
            }

            onNodeWithText("Home for Sheet").assertIsDisplayed()
            onNodeWithText("Open Sheet").performClick()

            onNodeWithText("Inside Bottom Sheet").assertIsDisplayed()
            onNodeWithText("Dismiss Sheet").performClick()

            onNodeWithText("Home for Sheet").assertIsDisplayed()
        }
}

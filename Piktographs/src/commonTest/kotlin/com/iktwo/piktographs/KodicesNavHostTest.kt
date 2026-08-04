package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
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
}

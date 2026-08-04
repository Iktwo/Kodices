package com.iktwo.piktographs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.iktwo.kodices.KodicesParser
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class PageUITest {
    @Test
    fun rendersTextFromParsedContent() =
        runComposeUiTest {
            val content = KodicesParser().parseJSONToContent(
                """
                {
                  "elements": [
                    { "type": "row", "id": "greeting", "text": "Hello" }
                  ]
                }
                """.trimIndent(),
            )

            assertNotNull(content)

            setContent {
                PageUI(content = content, elementOverrides = { false })
            }

            onNodeWithText("Hello").assertIsDisplayed()
        }
}

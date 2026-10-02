package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.sampleapp.ui.elementOverride
import com.iktwo.piktographs.navigation.KodicesNavHost
import com.iktwo.piktographs.navigation.rememberKodicesNavController
import com.iktwo.piktographs.ui.ContentDialog
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

@Composable
fun TabCatalog(contentString: String) {
    val dataString = json.encodeToString(JsonElement.serializer(), buildJsonObject { })

    var dialogMessage by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    val actionPerformer = object : ActionPerformer {
        override fun onAction(action: Action) {
            when (action) {
                is MessageAction -> {
                    dialogMessage = action.text
                    isDialogOpen = true
                }

                else -> {
                    sampleLogger.warn("Unhandled action $action")
                }
            }
        }
    }

    val navController = rememberKodicesNavController(initialRoute = "catalog")

    val fetcher: suspend (String) -> Content? = { route ->
        when (route) {
            "catalog" -> kodicesParser.parseJSONToContent(contentString, dataString)
            "subpage_demo" -> kodicesParser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "row",
                      "constants": {
                        "text": "Server-Driven Sub-Page",
                        "textSecondary": "Navigated seamlessly using KodicesNavHost!"
                      }
                    },
                    {
                      "type": "card",
                      "constants": {
                        "text": "Sub-Page Card Container",
                        "textSecondary": "This page was dynamically fetched and pushed onto the navigation stack.",
                        "variant": "outlined"
                      }
                    },
                    {
                      "type": "button",
                      "constants": {
                        "text": "Go Back to Main Catalog",
                        "variant": "filled"
                      },
                      "action": {
                        "type": "back"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
            "dialog_demo" -> kodicesParser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "row",
                      "constants": {
                        "text": "Server-Driven Modal Dialog",
                        "textSecondary": "Presented via ContentDialog across JVM, Android, iOS, and Web."
                      }
                    },
                    {
                      "type": "button",
                      "constants": {
                        "text": "Close Dialog",
                        "variant": "filled"
                      },
                      "action": {
                        "type": "dismiss_modal"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
            "sheet_demo" -> kodicesParser.parseJSONToContent(
                """
                {
                  "elements": [
                    {
                      "type": "row",
                      "constants": {
                        "text": "Server-Driven Bottom Sheet",
                        "textSecondary": "Presented via ModalBottomSheet across Compose Multiplatform."
                      }
                    },
                    {
                      "type": "button",
                      "constants": {
                        "text": "Dismiss Sheet",
                        "variant": "outlined"
                      },
                      "action": {
                        "type": "back"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
            else -> null
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        KodicesNavHost(
            navController = navController,
            fetchContent = fetcher,
            modifier = Modifier.fillMaxSize(),
            elementOverrides = { elementOverride(it) },
            actionPerformer = actionPerformer,
        )

        if (isDialogOpen) {
            ContentDialog(onCloseRequest = { isDialogOpen = false }) {
                Text(dialogMessage)
            }
        }
    }
}

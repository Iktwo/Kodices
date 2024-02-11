package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iktwo.kodices.Kodices
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.sampleapp.ui.ElementOverride
import com.iktwo.piktographs.PageUI
import com.iktwo.piktographs.ui.ContentDialog
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

@Composable
fun TabCatalog(contentString: String) {
    val dataString = json.encodeToString(JsonElement.serializer(), buildJsonObject { })
    Kodices.debug = true

    var dialogMessage by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    kodices.parseJSONToContent(contentString, dataString)?.let { content ->
        val actionPerformer = object : ActionPerformer {
            override fun onAction(action: Action) {
                when (action) {
                    is MessageAction -> {
                        dialogMessage = action.text
                        isDialogOpen = true
                    }

                    else -> {
                        Kodices.logger.warn("Unhandled action $action")
                    }
                }
            }
        }

        CompositionLocalProvider(DefaultActionPerformer provides actionPerformer) {
            PageUI(
                content = content,
                modifier = Modifier.fillMaxSize(),
                elementOverrides = {
                    ElementOverride(it)
                },
            )

            if (isDialogOpen) {
                ContentDialog(onCloseRequest = {
                    isDialogOpen = false
                }) {
                    Text(dialogMessage)
                }
            }
        }
    }
}

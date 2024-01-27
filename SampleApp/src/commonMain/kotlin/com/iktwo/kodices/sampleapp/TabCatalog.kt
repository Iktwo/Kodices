package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.iktwo.kodices.Kodices
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.piktographs.PageUI
import com.iktwo.kodices.sampleapp.ui.ElementOverride
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

@Composable
fun TabCatalog(contentString: String) {
    val dataString = json.encodeToString(JsonElement.serializer(), buildJsonObject { })
    Kodices.debug = true
    kodices.parseJSONToContent(contentString, dataString)?.let { content ->
        val actionPerformer = object : ActionPerformer {
            override fun onAction(action: Action) {
                // no-op
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
        }
    }
}

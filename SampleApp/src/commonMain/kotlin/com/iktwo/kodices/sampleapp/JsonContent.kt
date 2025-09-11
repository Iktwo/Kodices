package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.PageUI

@Composable
fun JsonContent(
    contentString: String,
    dataString: String,
    modifier: Modifier = Modifier,
    actionPerformer: ActionPerformer? = null,
    elementOverrides: (@Composable (ProcessedElement) -> Boolean)? = null,
    textInputData: SnapshotStateMap<String, String?> = rememberSaveable {
        mutableStateMapOf()
    },
    booleanInputData: SnapshotStateMap<String, Boolean> = rememberSaveable {
        mutableStateMapOf()
    },
    validityMap: SnapshotStateMap<String, Boolean> = rememberSaveable {
        mutableStateMapOf()
    },
    onInputIdsPopulated: () -> Unit = { },
    onInputUpdated: () -> Unit = { },
) {
    kodicesParser.parseJSONToContent(contentString, dataString)?.let { content ->
        CompositionLocalProvider(DefaultActionPerformer provides (actionPerformer ?: emptyActionPerformer)) {
            PageUI(
                content = content,
                modifier = modifier,
                elementOverrides = elementOverrides ?: { _ -> false },
                textInputData = textInputData,
                booleanInputData = booleanInputData,
                validityMap = validityMap,
                onInputIdsPopulated = onInputIdsPopulated,
                onInputUpdated = onInputUpdated,
            )
        }
    }
}

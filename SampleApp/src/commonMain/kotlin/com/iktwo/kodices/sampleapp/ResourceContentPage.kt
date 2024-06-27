package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.sampleapp.actions.WakeOnLANAction
import com.iktwo.kodices.sampleapp.actions.WakeOnLan
import com.iktwo.kodices.sampleapp.resources.Res
import com.iktwo.kodices.sampleapp.ui.ElementOverride
import com.iktwo.piktographs.PageUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ResourceContentPage(resourceFilename: String, dataFilename: String?) {
    val scope = rememberCoroutineScope()
    var contentString by remember { mutableStateOf("") }
    var dataString by remember { mutableStateOf("") }

    scope.launch(Dispatchers.IO) {
        val result = Res.readBytes("files/$resourceFilename").decodeToString()
        withContext(Dispatchers.Main) {
            contentString = result
        }
    }

    if (dataFilename != null) {
        scope.launch(Dispatchers.IO) {
            val result = Res.readBytes("files/$dataFilename").decodeToString()
            withContext(Dispatchers.Main) {
                dataString = result
            }
        }
    }

    val textInputData = remember {
        mutableStateMapOf<String, String?>()
    }

    val booleanInputData = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val validityMap = remember {
        mutableStateMapOf<String, Boolean>()
    }

    if (contentString.isNotBlank()) {
        kodices.parseJSONToContent(contentString, dataString)?.let { content ->
            val actionPerformer = object : ActionPerformer {
                override fun onAction(action: Action) {
                    when (action) {
                        is WakeOnLANAction -> {
                            val port =
                                textInputData.getOrElse(action.portFieldName) { "0" }?.toIntOrNull()
                                    ?: 0
                            val ip = textInputData.getOrElse(action.ipFieldName) { "" } ?: ""
                            val macAddress =
                                textInputData.getOrElse(action.macFieldName) { "" } ?: ""

                            if (macAddress.isNotBlank()) {
                                WakeOnLan.wakeDevice(macAddress, ip, port)
                            } else {
                                // TODO: show error message
                            }
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
                    textInputData = textInputData,
                    booleanInputData = booleanInputData,
                    validityMap = validityMap,
                    onInputIdsPopulated = {
                        scope.launch {
                            val restored = restoreForm(textInputData.keys)
                            restored.filter { (_, value) -> value != null }
                                .forEach { (key, value) ->
                                    textInputData[key] = value
                                }
                            // TODO: restoring after coming back on this "page" clears one of the fields. Something must be wrong.
                        }
                    },
                    onInputUpdated = {
                        scope.launch {
                            val validValues = textInputData.mapNotNull { (key, value) ->
                                if (validityMap.containsKey(key) && value != null) key to value else null
                            }.toMap()

                            if (validValues.isNotEmpty()) {
                                saveForm(validValues)
                            }
                        }
                    }
                )
            }
        }
    }
}

suspend fun saveForm(formData: Map<String, String>) {
    dataStore.edit { preferences ->
        formData.map { (key, value) ->
            Pair(stringPreferencesKey(key), value)
        }.forEach { (key, value) ->
            preferences[key] = value
        }
    }
}

suspend fun restoreForm(keys: Set<String>): Map<String, String?> {
    return dataStore.data.map { preferences ->
        keys.map { key ->
            key to preferences[stringPreferencesKey(key)]
        }.filter { (_, value) -> value != null }
    }.stateIn(CoroutineScope(Dispatchers.Default)).value.toMap()
}

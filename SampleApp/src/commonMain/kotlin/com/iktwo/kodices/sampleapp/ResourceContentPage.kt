package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.sampleapp.actions.WakeOnLANAction
import com.iktwo.kodices.sampleapp.actions.WakeOnLan
import com.iktwo.kodices.sampleapp.resources.Res
import com.iktwo.kodices.sampleapp.ui.elementOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ResourceContentPage(
    resourceFilename: String,
    dataFilename: String?,
) {
    val scope = rememberCoroutineScope()
    var contentString by remember { mutableStateOf("") }
    var dataString by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // These have to be effects: launching from the composition body sets state, which recomposes,
    // which launches again, re-reading the file for as long as this composable is on screen.
    LaunchedEffect(resourceFilename) {
        contentString = withContext(Dispatchers.IO) {
            Res.readBytes("files/$resourceFilename").decodeToString()
        }
    }

    LaunchedEffect(dataFilename) {
        dataString = if (dataFilename == null) {
            ""
        } else {
            withContext(Dispatchers.IO) {
                Res.readBytes("files/$dataFilename").decodeToString()
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
                            errorMessage = null
                            scope.launch {
                                runCatching { WakeOnLan.wakeDevice(macAddress, ip, port) }
                                    .onFailure { errorMessage = "Failed to send the Wake-on-LAN packet: ${it.message}" }
                            }
                        } else {
                            errorMessage = "Enter a MAC address before sending a Wake-on-LAN packet."
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }

            JsonContent(
                contentString = contentString,
                dataString = dataString,
                actionPerformer = actionPerformer,
                textInputData = textInputData,
                booleanInputData = booleanInputData,
                validityMap = validityMap,
                elementOverrides = {
                    elementOverride(it)
                },
                onInputIdsPopulated = {
                    scope.launch {
                        // Snapshot the keys first: restoreForm suspends, and writing back into
                        // textInputData while iterating its live key set would be a concurrent modification.
                        val restored = restoreForm(textInputData.keys.toSet())
                        restored
                            .filter { (_, value) -> value != null }
                            .forEach { (key, value) ->
                                textInputData[key] = value
                            }
                    }
                },
                onInputUpdated = {
                    scope.launch {
                        val validValues = textInputData
                            .mapNotNull { (key, value) ->
                                if (validityMap.containsKey(key) && value != null) key to value else null
                            }.toMap()

                        if (validValues.isNotEmpty()) {
                            saveForm(validValues)
                        }
                    }
                },
            )
        }
    }
}

suspend fun saveForm(formData: Map<String, String>) {
    dataStore.edit { preferences ->
        formData
            .map { (key, value) ->
                Pair(stringPreferencesKey(key), value)
            }.forEach { (key, value) ->
                preferences[key] = value
            }
    }
}

suspend fun restoreForm(keys: Set<String>): Map<String, String?> {
    // `first()`, not `stateIn(CoroutineScope(...))`: the latter created a scope that was never
    // cancelled, leaking a DataStore collector on every call.
    return dataStore.data
        .map { preferences ->
            keys
                .map { key ->
                    key to preferences[stringPreferencesKey(key)]
                }.filter { (_, value) -> value != null }
        }.first()
        .toMap()
}

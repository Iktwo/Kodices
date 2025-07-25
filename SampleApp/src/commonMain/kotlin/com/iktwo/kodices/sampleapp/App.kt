package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.Kodices
import com.iktwo.kodices.sampleapp.actions.WakeOnLANAction
import com.iktwo.kodices.sampleapp.resources.Res
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.WebElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

val json = Json { prettyPrint = true }
val kodices = Kodices(
    elements = listOf(WebElement, CountdownElement),
    actions = listOf(WakeOnLANAction),
)

enum class Tabs(
    val displayName: String,
) {
    TabSamples("Samples"),
    ComponentCatalog("Catalog"),

    //    Websockets("Websockets"),
    Input("Dynamic Input"),
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val orderedTabs = Tabs.entries
    var selectedTab by remember { mutableStateOf(orderedTabs.first()) }

    val sampleInitialContent = "{}"

    var activeContent by remember { mutableStateOf(sampleInitialContent) }
    var catalogContent by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    scope.launch(Dispatchers.IO) {
        val result = Res.readBytes("files/catalog.json").decodeToString()
        withContext(Dispatchers.Main) {
            catalogContent = result
        }
    }

    Column(modifier = Modifier.safeDrawingPadding().fillMaxSize()) {
        TabRow(selectedTabIndex = orderedTabs.indexOf(selectedTab)) {
            orderedTabs.map {
                Tab(selected = true, onClick = {
                    selectedTab = it
                }) {
                    Text(it.displayName, modifier = Modifier.padding(8.dp))
                }
            }
        }

        when (selectedTab) {
            Tabs.ComponentCatalog -> {
                TabCatalog(catalogContent)
            }

            Tabs.Input -> {
                TabInput(
                    initialContentString = activeContent,
                    onJSONTextChanged = { activeContent = it }
                )
            }

            Tabs.TabSamples -> {
                TabSamples()
            }

//            Tabs.Websockets -> {
//                TabWebsockets()
//            }
        }
    }
}

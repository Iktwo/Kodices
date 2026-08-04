package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.KodicesRegistry
import com.iktwo.kodices.sampleapp.actions.WakeOnLANAction
import com.iktwo.kodices.sampleapp.resources.Res
import com.iktwo.kodices.utils.Logger
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.WebElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

val json = Json { prettyPrint = true }

/** Where the sample reports parse failures and unhandled actions. */
val sampleLogger = object : Logger {
    override fun debug(message: String) = println("D: $message")

    override fun info(message: String) = println("I: $message")

    override fun warn(message: String) = println("W: $message")

    override fun error(message: String) = println("E: $message")
}

val kodicesParser = KodicesParser(
    registry = KodicesRegistry.of(
        elements = listOf(WebElement, CountdownElement),
        actions = listOf(WakeOnLANAction),
        // The sample registers everything it needs, so a miss is a bug rather than something to
        // paper over with the deprecated global registry.
        allowGlobalFallback = false,
        logger = sampleLogger,
    ),
    logger = sampleLogger,
)

enum class Tabs(
    val displayName: String,
) {
    TabSamples("Samples"),
    ComponentCatalog("Catalog"),
    Input("Dynamic Input"),
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val orderedTabs = Tabs.entries
    var selectedTab by remember { mutableStateOf(orderedTabs.first()) }

    val sampleInitialContent = "{}"

    var activeContent by remember { mutableStateOf(sampleInitialContent) }
    var activeData by remember { mutableStateOf(sampleInitialContent) }
    var catalogContent by remember { mutableStateOf("") }

    // Has to be an effect: launching from the composition body sets state, which recomposes,
    // which launches again, for as long as this composable is on screen.
    LaunchedEffect(Unit) {
        catalogContent = withContext(Dispatchers.IO) {
            Res.readBytes("files/catalog.json").decodeToString()
        }
    }

    Column(modifier = Modifier.safeDrawingPadding().fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = orderedTabs.indexOf(selectedTab)) {
            orderedTabs.map { tab ->
                Tab(selected = tab == selectedTab, onClick = {
                    selectedTab = tab
                }) {
                    Text(tab.displayName, modifier = Modifier.padding(8.dp))
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
                    initialDataString = activeData,
                    onJSONUIChanged = { activeContent = it },
                    onJSONDataChanged = { activeData = it },
                )
            }

            Tabs.TabSamples -> {
                TabSamples()
            }
        }
    }
}

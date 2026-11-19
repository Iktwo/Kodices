package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SecondaryScrollableTabRow
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
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.kodices.sampleapp.actions.WakeOnLANAction
import com.iktwo.kodices.sampleapp.resources.Res
import com.iktwo.kodices.utils.Logger
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.WebElement
import com.iktwo.piktographs.navigation.DismissModalAction
import com.iktwo.piktographs.navigation.ModalAction
import com.iktwo.piktographs.navigation.NavigateAction
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
        actions = listOf(
            WakeOnLANAction,
            MessageAction.descriptor,
            NavigateAction.descriptor,
            ModalAction.descriptor,
            DismissModalAction.descriptor,
        ),
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
    Dashboard("Dashboard"),
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(Tabs.entries.first()) }

    val sampleInitialContent = "{}"

    var activeContent by remember { mutableStateOf(sampleInitialContent) }
    var activeData by remember { mutableStateOf(sampleInitialContent) }
    var catalogContent by remember { mutableStateOf("") }

    // Before anything shows an image: Coil keeps the first loader it is given.
    remember { installDashboardImageLoader() }

    // Has to be an effect: launching from the composition body sets state, which recomposes,
    // which launches again, for as long as this composable is on screen.
    LaunchedEffect(Unit) {
        catalogContent = withContext(Dispatchers.IO) {
            Res.readBytes("files/catalog.json").decodeToString()
        }
    }

    AppScaffold(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) { tab ->
        when (tab) {
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

            Tabs.Dashboard -> {
                TabDashboard()
            }
        }
    }
}

/** The app's frame: the tab row, and under it whatever [content] draws for the selected tab. */
@Composable
internal fun AppScaffold(
    selectedTab: Tabs,
    onTabSelected: (Tabs) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Tabs) -> Unit,
) {
    Column(modifier = modifier.safeDrawingPadding().fillMaxSize()) {
        // Scrollable: four equal tabs are too narrow for their names on a phone, where
        // "Dashboard" broke in the middle of the word.
        SecondaryScrollableTabRow(selectedTabIndex = Tabs.entries.indexOf(selectedTab), edgePadding = 0.dp) {
            Tabs.entries.forEach { tab ->
                Tab(selected = tab == selectedTab, onClick = { onTabSelected(tab) }) {
                    Text(tab.displayName, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), maxLines = 1)
                }
            }
        }

        content(selectedTab)
    }
}

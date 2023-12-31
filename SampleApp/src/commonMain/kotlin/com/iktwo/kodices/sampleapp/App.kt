package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.Kodices
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.WebElement
import kotlinx.serialization.json.Json

val json = Json { prettyPrint = true }
val kodices = Kodices(elements = listOf(WebElement, CountdownElement))

enum class Tabs(val displayName: String) {
    CountdownDashboard("Countdown"),
    ComponentCatalog("Catalog"),
    Input("Dynamic Input"),
}

@Composable
fun App() {
    val orderedTabs = Tabs.entries
    var selectedTab by remember { mutableStateOf(orderedTabs.first()) }

    val sampleInitialContent = "{}"

    var activeContent by remember { mutableStateOf(sampleInitialContent) }

    Column(modifier = Modifier.fillMaxSize()) {
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
            Tabs.CountdownDashboard -> {
                TabCountdownDashboard()
            }

            Tabs.ComponentCatalog -> {
                TabCatalog(activeContent)
            }

            Tabs.Input -> {
                TabInput(activeContent) {
                    activeContent = it
                }
            }
        }
    }
}

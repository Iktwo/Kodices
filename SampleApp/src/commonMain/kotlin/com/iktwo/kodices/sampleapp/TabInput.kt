package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.sampleapp.ui.elementOverride

@Composable
fun TabInput(
    initialContentString: String,
    initialDataString: String,
    onJSONDataChanged: (String) -> Unit,
    onJSONUIChanged: (String) -> Unit,
) {
    var jsonContent by remember { mutableStateOf(initialContentString) }
    var jsonData by remember { mutableStateOf(initialDataString) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        if (isCompact) {
            var selectedSubTab by remember { mutableIntStateOf(0) }
            Column(modifier = Modifier.fillMaxSize()) {
                SecondaryTabRow(selectedTabIndex = selectedSubTab) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                    ) {
                        Text("Editor", modifier = Modifier.padding(8.dp))
                    }
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                    ) {
                        Text("Preview", modifier = Modifier.padding(8.dp))
                    }
                }

                if (selectedSubTab == 0) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            JSONTextEditor(
                                initialValue = initialContentString,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            ) {
                                jsonContent = it
                                onJSONUIChanged(it)
                            }
                        }

                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            JSONTextEditor(
                                title = "Data",
                                initialValue = initialDataString,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            ) {
                                jsonData = it
                                onJSONDataChanged(it)
                            }
                        }
                    }
                } else {
                    JsonContent(
                        contentString = jsonContent,
                        dataString = jsonData,
                        elementOverrides = { elementOverride(it) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        JSONTextEditor(
                            initialValue = initialContentString,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        ) {
                            jsonContent = it
                            onJSONUIChanged(it)
                        }
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        JSONTextEditor(
                            title = "Data",
                            initialValue = initialDataString,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        ) {
                            jsonData = it
                            onJSONDataChanged(it)
                        }
                    }
                }

                JsonContent(
                    contentString = jsonContent,
                    dataString = jsonData,
                    elementOverrides = { elementOverride(it) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

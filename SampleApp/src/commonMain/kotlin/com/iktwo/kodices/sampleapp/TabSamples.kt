package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TabSamples() {
    Surface(Modifier.fillMaxSize()) {
        var selectedSample by remember { mutableStateOf<Samples?>(null) }

        when (selectedSample) {
            null -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    Samples.entries.forEach { sample ->
                        item {
                            Button(modifier = Modifier.padding(8.dp), onClick = {
                                selectedSample = sample
                            }) {
                                Text(sample.displayName)
                            }
                        }
                    }
                }
            }

            else -> {
                ResourceContentPage(selectedSample?.resourceFilename ?: "")
            }
        }
    }


}

enum class Samples(val displayName: String, val resourceFilename: String) {
    WakeOnLan("Wake On LAN", resourceFilename = "wol.json")
}
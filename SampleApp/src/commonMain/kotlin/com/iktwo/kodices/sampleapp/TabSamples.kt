package com.iktwo.kodices.sampleapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
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

        AnimatedContent(selectedSample, transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }.using(
                SizeTransform(clip = false)
            )
        }) { sample ->
            when (sample) {
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = { selectedSample = null }) {
                            Text(text = "Back")
                        }

                        ResourceContentPage(
                            selectedSample?.resourceFilename ?: "",
                            selectedSample?.dataFilename
                        )
                    }
                }
            }
        }
    }


}

enum class Samples(
    val displayName: String,
    val resourceFilename: String,
    val dataFilename: String? = null
) {
    WakeOnLan("Wake On LAN", resourceFilename = "wol.json"),
    Countdown(
        "Countdown",
        resourceFilename = "countdown.json",
        dataFilename = "data_countdown.json"
    )
}
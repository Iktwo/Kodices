package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.SimpleAction
import com.iktwo.kodices.utils.Constants
import com.iktwo.piktographs.PageUI
import com.iktwo.piktographs.data.Countdown
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.ui.ContentDialog
import com.iktwo.kodices.sampleapp.data.InMemoryCountdownRepository
import com.iktwo.kodices.sampleapp.ui.ElementOverride
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

val emptyActionPerformer = object : ActionPerformer {
    override fun onAction(action: Action) {
    }
}

val DefaultActionPerformer = staticCompositionLocalOf { emptyActionPerformer }

val LastSecond = compositionLocalOf { Clock.System.now() }

val inMemoryCountdownRepository = InMemoryCountdownRepository()

data class ContentContainer(val content: JsonElement, val data: JsonElement? = null)

@Composable
fun TabCountdownDashboard() {
    val model by remember { mutableStateOf(inMemoryCountdownRepository) }

    var state by remember { mutableStateOf(ContentContainer(buildJsonObject { })) }

    var name by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (model.isEmpty()) {
            listOf(
                Countdown(
                    name = "Retirement",
                    null,
                    LocalDateTime.parse("2057-01-01T00:00:00"),
                ),
                Countdown(
                    name = "2030",
                    null,
                    LocalDateTime.parse("2030-01-01T00:00:00"),
                ),
                Countdown(
                    name = "Cinco de Mayo 2028",
                    null,
                    LocalDateTime.parse("2028-05-05T00:00:00"),
                ),
                Countdown(
                    name = "2000",
                    null,
                    LocalDateTime.parse("2000-01-01T00:00:00"),
                ),
            ).forEach {
                model.add(it)
            }
        }

        val entries = json.encodeToJsonElement(ListSerializer(Countdown.serializer()), model.get())

        state = state.copy(
            content = buildJsonObject {
                putJsonArray("elements") {
                    addJsonObject {
                        put("type", "countdown")

                        putJsonObject(Constants.CONSTANTS) {
                            put(CountdownElement.STYLE, "short")
                        }

                        putJsonObject("processors") {
                            putJsonObject("target") {
                                put("type", "path")
                                put("element", "target")
                            }

                            putJsonObject("text") {
                                put("type", "path")
                                put("element", "name")
                            }
                        }

                        putJsonObject("expandWithProcessor") {
                            put("type", "path")
                            put("element", "entries")
                        }
                    }

                    addJsonObject {
                        put("type", "button")
                        putJsonObject("constants") {
                            put("text", "Add")
                        }

                        putJsonObject("action") {
                            put("type", "add")
                        }
                    }
                }
            },
            data = buildJsonObject {
                put("entries", entries)
            },
        )
    }

    kodices.parseJSONElementToContent(state.content, state.data)?.let { content ->
        println("New content size: ${content.elements.size} content: ${state.content}")
        var openDialog by remember { mutableStateOf(false) }

        fun closeAndClear() {
            openDialog = false
            name = ""
        }

        if (openDialog) {
            ContentDialog(onCloseRequest = {
                closeAndClear()
            }) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // TODO: add datetime picker
                    Column(modifier = Modifier.padding(8.dp)) {
                        TextField(value = name, onValueChange = {
                            name = it
                        }, placeholder = {
                            Text("Name")
                        })

                        Button(onClick = {
                            if (name.isBlank()) {
                                return@Button
                            }

                            scope.launch {
                                model.add(
                                    Countdown(
                                        name = name,
                                        target = LocalDateTime.parse("2025-01-01T16:00"),
                                    ),
                                )

                                val entries = json.encodeToJsonElement(
                                    ListSerializer(Countdown.serializer()),
                                    model.get(),
                                )

                                state = state.copy(
                                    data = buildJsonObject {
                                        put("entries", entries)
                                    },
                                )

                                closeAndClear()
                            }
                        }) {
                            Text("Add")
                        }

                        Button(onClick = {
                            closeAndClear()
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        val actionPerformer = object : ActionPerformer {
            override fun onAction(action: Action) {
                when {
                    action.type == "add" -> {
                        openDialog = true
                    }

                    action is SimpleAction -> {
                        openDialog = true
                    }

                    else -> {
                    }
                }
            }
        }

        var time by remember { mutableStateOf(Clock.System.now()) }

        if (content.elements.firstOrNull { it is CountdownElement } != null) {
            LaunchedEffect(Unit) {
                while (true) {
                    time = Clock.System.now()

                    delay(1000 - (Clock.System.now().nanosecondsOfSecond / 1000000L))
                }
            }
        }

        CompositionLocalProvider(DefaultActionPerformer provides actionPerformer) {
            CompositionLocalProvider(LastSecond provides time) {
                PageUI(
                    content = content,
                    modifier = Modifier.fillMaxSize(),
                    elementOverrides = {
                        ElementOverride(it)
                    },
                )
            }
        }
    }
}

package com.iktwo.kodices.sampleapp.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.iktwo.piktographs.elements.WebElement

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebUI(element: WebElement) {
    val state = rememberWebViewState(element.url)
    val client =
        object : AccompanistWebViewClient() {
            override fun onPageFinished(
                view: WebView,
                url: String?,
            ) {
                super.onPageFinished(view, url)
                element.jsOnLoad?.let { js ->
                    view.evaluateJavascript(js) { _ -> }
                }
            }
        }

    WebView(
        state = state,
        onCreated = {
            it.settings.javaScriptEnabled = true
        },
        modifier = Modifier.fillMaxWidth().height(400.dp),
        client = client,
    )
}

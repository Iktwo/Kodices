package com.iktwo.kodices.sampleapp.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.iktwo.piktographs.elements.WebElement

/*
 * The platform WebView in an AndroidView. No @Preview: a WebView does not draw in one, and what it
 * shows is the page at the element's url.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebUI(element: WebElement) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
            }
        },
        update = { view ->
            view.webViewClient = object : WebViewClient() {
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

            // update runs on every recomposition; only a new address is a reason to load again.
            if (view.tag != element.url) {
                view.tag = element.url
                view.loadUrl(element.url)
            }
        },
        onRelease = { view -> view.destroy() },
    )
}

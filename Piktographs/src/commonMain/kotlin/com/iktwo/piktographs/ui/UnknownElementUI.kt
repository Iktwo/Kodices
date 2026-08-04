package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement

public enum class UnknownElementPlaceholder {
    /** Render nothing at all. */
    Nothing,

    /** Render the element type. */
    Message,

    /** Render the element type and its parsed contents. Useful while authoring JSON. */
    Verbose,
}

/**
 * Controls what [UnknownElementUI] renders for an element type the renderer does not know.
 *
 * Defaults to [UnknownElementPlaceholder.Message] so an unsupported type is visible rather than
 * silently missing. Provide [UnknownElementPlaceholder.Nothing] if a blank space is preferred.
 */
public val LocalUnknownElementPlaceholder: androidx.compose.runtime.ProvidableCompositionLocal<UnknownElementPlaceholder> =
    staticCompositionLocalOf { UnknownElementPlaceholder.Message }

/**
 * Rendered when neither a built-in renderer nor an override handles an element's type.
 *
 * This used to be gated on the global `KodicesParser.debug`, which meant an unsupported element
 * rendered nothing at all in release builds - a blank screen with no indication why.
 */
@Composable
public fun UnknownElementUI(processedElement: ProcessedElement) {
    val text = when (LocalUnknownElementPlaceholder.current) {
        UnknownElementPlaceholder.Nothing -> return
        UnknownElementPlaceholder.Message -> "\"${processedElement.type}\" is not a supported element"
        UnknownElementPlaceholder.Verbose -> "\"${processedElement.type}\" is not a supported element. $processedElement"
    }

    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

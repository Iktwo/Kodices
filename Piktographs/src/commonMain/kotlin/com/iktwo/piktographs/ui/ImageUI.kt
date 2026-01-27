package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import coil3.compose.AsyncImage
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull

const val IMAGE_ELEMENT_TYPE = "image"

const val KEY_URL = "url"
const val KEY_CONTENT_DESCRIPTION = "contentDescription"

@Composable
fun ImageUI(element: ProcessedElement) {
    // TODO: How do we expose custom properties in ProcessedElement?
    //  without jsonValues? For example, if we wanted a "URL" in here.
    //  Does having an interface like URLProvider that looks for KEY_URL makes sense?
    AsyncImage(
        model = element.jsonValues[KEY_URL]?.asStringOrNull(),
        contentDescription = element.jsonValues[KEY_CONTENT_DESCRIPTION]?.asStringOrNull(),
    )
}

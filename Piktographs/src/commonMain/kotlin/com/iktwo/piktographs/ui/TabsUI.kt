package com.iktwo.piktographs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val TABS_ELEMENT_TYPE: String = "tabs"

private const val KEY_ICON = "icon"
private const val KEY_SELECTED = "selected"

/**
 * A row of tabs: the links between the screens of an app, with the current one marked.
 *
 * Each nested element is a tab, whatever its `type` (use `tab`):
 * - `text`: the label.
 * - `icon`: an icon name (see [IconUI]) before the label.
 * - `selected`: `true` on the tab of the screen being shown. Tabs do not switch anything by
 *   themselves: each screen marks its own.
 * - its actions run when it is tapped, usually `navigate`.
 *
 * The row scrolls sideways when the tabs do not fit. As a top-level element of a page, `PageUI`
 * keeps it above the scrolling content, under the top bar.
 */
@Composable
public fun TabsUI(element: ProcessedElement) {
    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
        ) {
            element.nestedElements.forEach { tab ->
                val selected = tab.booleanValue(KEY_SELECTED) == true
                val color = if (selected) scheme.primary else scheme.onSurfaceVariant

                // On a narrow screen the current tab can be past the edge: scroll it into sight,
                // or the row says nothing about where the user is.
                val bringIntoView = remember { BringIntoViewRequester() }
                if (selected) {
                    LaunchedEffect(tab.id) { bringIntoView.bringIntoView() }
                }

                // IntrinsicSize.Max so the indicator is as wide as its label, not the label as wide
                // as an indicator that fills.
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .bringIntoViewRequester(bringIntoView)
                        .selectable(
                            selected = selected,
                            enabled = isEnabled && tab.enabled,
                            role = Role.Tab,
                            onClick = { tab.actions.forEach { actionPerformer.onAction(it) } },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.height(TAB_HEIGHT).padding(horizontal = TAB_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(TAB_ICON_GAP),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tab.stringValue(KEY_ICON)?.let(::resolveImageVector)?.let {
                            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(TAB_ICON_SIZE), tint = color)
                        }
                        tab.text?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else null,
                                color = color,
                                maxLines = 1,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TAB_PADDING)
                            .height(INDICATOR_HEIGHT)
                            .background(if (selected) scheme.primary else Color.Transparent, INDICATOR_SHAPE),
                    )
                }
            }
        }

        HorizontalDivider(color = scheme.outlineVariant)
    }
}

private val TAB_HEIGHT = 44.dp
private val TAB_PADDING = 16.dp
private val TAB_ICON_GAP = 8.dp
private val TAB_ICON_SIZE = 18.dp
private val INDICATOR_HEIGHT = 3.dp
private val INDICATOR_SHAPE = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)

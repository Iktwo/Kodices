package com.iktwo.piktographs.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

enum class TopBarStyle {
    Small,
    Medium,
    Large,
    ;

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun scrollBehavior(state: TopAppBarState): TopAppBarScrollBehavior {
        return when (this) {
            Small -> {
                TopAppBarDefaults.pinnedScrollBehavior(state)
            }

            Medium -> {
                TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
            }

            Large -> {
                TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
            }
        }
    }

    companion object {
        fun fromText(style: String?): TopBarStyle {
            return entries.firstOrNull { it.name.equals(style, ignoreCase = true) } ?: Small
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopBar(
    title: String?,
    style: TopBarStyle = TopBarStyle.Small,
    scrollBehavior: TopAppBarScrollBehavior,
    color: Color = MaterialTheme.colorScheme.surface,
    scrolledColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    actions: @Composable RowScope.() -> Unit = {},
    navigationEnabled: Boolean = false,
    onNavigationIconClick: (() -> Unit)? = null,
) {
    val targetFraction = if (style == TopBarStyle.Small) {
        scrollBehavior.state.overlappedFraction
    } else {
        scrollBehavior.state.collapsedFraction
    }

    val smoothedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TopAppBarColorTransition_${title ?: ""}",
    )

    val transitionColor = lerp(color, scrolledColor, smoothedFraction)

    val appBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = transitionColor,
        scrolledContainerColor = transitionColor,
    )

    val titleContent: @Composable () -> Unit = {
        title?.let { Text(it) }
    }

    when (style) {
        TopBarStyle.Small -> {
            TopAppBar(
                title = titleContent,
                scrollBehavior = scrollBehavior,
                actions = actions,
                colors = appBarColors,
                navigationIcon = {
                    if (navigationEnabled) {
                        IconButton(onClick = onNavigationIconClick ?: {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        }

        TopBarStyle.Medium -> {
            MediumTopAppBar(
                title = titleContent,
                scrollBehavior = scrollBehavior,
                colors = appBarColors,
            )
        }

        TopBarStyle.Large -> {
            LargeTopAppBar(
                title = titleContent,
                scrollBehavior = scrollBehavior,
                colors = appBarColors,
            )
        }
    }
}

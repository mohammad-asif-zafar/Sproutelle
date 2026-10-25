package com.hathway.sproutelle.core.responsive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSizeClass {
    Compact,   // Phones (< 600dp)
    Medium,    // Small tablets / Foldables (600dp - 840dp)
    Expanded   // Large tablets / Desktop (> 840dp)
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val widthDp: Dp
) {
    val isTablet: Boolean
        get() = widthSizeClass != WindowWidthSizeClass.Compact
}

@Composable
fun ProvideWindowSizeClass(content: @Composable (WindowSizeClass) -> Unit) {
    BoxWithConstraints {
        val width = maxWidth
        val widthClass = when {
            width < 600.dp -> WindowWidthSizeClass.Compact
            width < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
        val windowSizeClass = WindowSizeClass(widthClass, width)
        content(windowSizeClass)
    }
}

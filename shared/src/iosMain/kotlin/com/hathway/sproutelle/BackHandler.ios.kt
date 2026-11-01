package com.hathway.sproutelle

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses platform gesture navigation
}

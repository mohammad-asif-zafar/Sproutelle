package com.hathway.sproutelle

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(enabled: Boolean = true, onBack: () -> Unit)

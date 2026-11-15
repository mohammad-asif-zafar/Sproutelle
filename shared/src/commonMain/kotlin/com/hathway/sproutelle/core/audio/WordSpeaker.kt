package com.hathway.sproutelle.core.audio

import androidx.compose.runtime.Composable

@Composable
expect fun rememberWordSpeaker(): (String) -> Unit

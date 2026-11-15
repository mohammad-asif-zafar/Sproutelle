package com.hathway.sproutelle.core.audio

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun rememberWordSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    var isReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                engine?.setSpeechRate(0.9f) // Slightly slower, kid-friendly pacing
                isReady = true
            }
        }
        engine
    }

    DisposableEffect(tts) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    return remember(tts, isReady) {
        { text: String ->
            if (isReady && text.isNotBlank()) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
            }
        }
    }
}

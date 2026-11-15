package com.hathway.sproutelle.core.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

@Composable
actual fun rememberWordSpeaker(): (String) -> Unit {
    val synthesizer = remember { AVSpeechSynthesizer() }

    DisposableEffect(synthesizer) {
        onDispose {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }

    return remember(synthesizer) {
        { text: String ->
            if (text.isNotBlank()) {
                if (synthesizer.isSpeaking()) {
                    synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
                }
                val utterance = AVSpeechUtterance(string = text).apply {
                    voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
                    rate = 0.45f // Kid-friendly clear pronunciation speed
                }
                synthesizer.speakUtterance(utterance)
            }
        }
    }
}

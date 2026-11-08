package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A highly interactive, 3D comic-sticker style Audio Button designed for toddlers (3+ years old).
 * Emits a continuous soft pulsating layout wave radiating outward when audio is active.
 */
@Composable
fun SproutelleAudioButton(
    isMuted: Boolean,
    isPlaying: Boolean, // Added state handle to toggle the pulsating visual soundwave effect
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp) // Large hit target margin metrics
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile toy bounce response compression modifier logic
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "AudioButtonScale"
    )

    // Continuous Easing Wave Loops for Pulsating Soundwave Ripples
    val infiniteTransition = rememberInfiniteTransition(label = "SoundwaveEffectLoop")

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveScale"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveAlpha"
    )

    val shadowOffset = if (buttonSize >= 56.dp) 4.dp else 3.dp

    Box(
        modifier = modifier
            .padding(contentPadding) // Child-safe touch target boundaries
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(buttonSize + (if (buttonSize >= 56.dp) 48.dp else 32.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Continuous Outward Pulsating Soft Soundwave Rings
            if (isPlaying && !isMuted) {
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .graphicsLayer {
                            scaleX = rippleScale
                            scaleY = rippleScale
                            alpha = rippleAlpha
                        }
                        .background(Color(0xFF29B6F6).copy(alpha = 0.5f), shape = CircleShape)
                )
            }

            // Layer 2: Main Button Footprint Frame with 3D drop shadow offset rim
            Box(modifier = Modifier.size(buttonSize + shadowOffset)) {
                // Background dark cyan structural offset path layout shadow disc
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = if (isMuted) Color(0xFFE65100).copy(alpha = 0.4f) else Color(0xFFB2EBF2),
                            shape = CircleShape
                        )
                )

                // Main Core Foreground Core Disc Container
                Surface(
                    modifier = Modifier
                        .size(buttonSize)
                        .align(Alignment.TopCenter)
                        .border(
                            width = if (buttonSize >= 56.dp) 3.dp else 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isMuted) {
                                        listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
                                    } else {
                                        listOf(Color(0xFF29B6F6), Color(0xFF0288D1))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = "Audio Spech Stream Trigger",
                            tint = Color.White,
                            modifier = Modifier.size(buttonSize * 0.55f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STUDIO PREVIEWS
// ============================================================================

@Preview(name = "Mobile Sizing Viewport - Audio Wave Radiating", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun AudioButtonMobileActivePreview() {
    Box(modifier = Modifier.padding(32.dp)) {
        SproutelleAudioButton(isMuted = false, isPlaying = true, onClick = {})
    }
}

@Preview(name = "Tablet Sizing Viewport - Muted Wave Suppressed", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun AudioButtonTabletMutedPreview() {
    Box(modifier = Modifier.padding(32.dp)) {
        SproutelleAudioButton(isMuted = true, isPlaying = true, buttonSize = 64.dp, onClick = {})
    }
}

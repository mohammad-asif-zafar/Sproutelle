package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A highly interactive, tactile Home Button designed for toddlers (3+ years old).
 * Mimics the clean white disc container with a deep blue modern icon layer and
 * safe oversized tap thresholds.
 */
@Composable
fun SproutelleHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    buttonSize: androidx.compose.ui.unit.Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy scale compression animation for little fingers when pushing the button
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HomeButtonScale"
    )

    val iconSize = (buttonSize.value * 32f / 56f).dp

    Box(
        modifier = modifier
            // Expands the outer touch landing zone without expanding the visual size
            .padding(contentPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom spring handles visual state, removes grey ripple bounds
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Core structural button white container disc with custom soft elevation shadow
        Surface(
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Spherically rounded primary internal layout layer matching the design tone
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Go Home Navigation",
                    tint = Color(0xFF007AFF), // Vibrant modern layout accent blue matching image asset
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

// ==========================================
// STUDIO PREVIEW
// ==========================================

@Preview(name = "Home Button Preview", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun SproutelleHomeButtonPreview() {
    Box(
        modifier = Modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        SproutelleHomeButton(onClick = {})
    }
}

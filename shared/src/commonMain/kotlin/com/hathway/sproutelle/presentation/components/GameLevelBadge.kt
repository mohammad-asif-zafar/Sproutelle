package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * An improved, highly playful Game Level Badge tailored for children aged 3+.
 * Featuring a thick comic-style border, layered drop-shadow offset depth effect,
 * and a bouncy spring response layout scale when children interact with it.
 */
@Composable
fun GameLevelBadge(
    levelText: String,
    icon: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFF493), // Soft bright yellow matching design base
    borderColor: Color = Color(0xFFFBC02D),     // Darker yellow accent border line
    textColor: Color = Color(0xFF5D4037),        // Deep earthy brown tone for rich text contrast
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Playful spring-loaded scaling physics for Pre-K child interaction feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BadgeScale"
    )

    val shape = RoundedCornerShape(24.dp)
    val shadowOffset = 4.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null, // Disable default gray ripples to preserve cartoony look
                        onClick = onClick
                    )
                } else Modifier
            )
            // Draws a solid graphic comic-style drop shadow behind the component container shape
            .drawBehind {
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, shadowOffset.toPx()),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                )
            }
    ) {
        Surface(
            shape = shape,
            color = backgroundColor,
            modifier = Modifier.drawBehind {
                // Thick 3.dp outer graphic cartoon boundary stroke line
                drawRoundRect(
                    color = borderColor,
                    size = size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                )
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Playful pop emoji asset icon
                Text(
                    text = icon,
                    fontSize = 16.sp
                )

                // High-visibility extra-bold text configuration
                Text(
                    text = levelText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ==========================================
// STUDIO PREVIEWS
// ==========================================

@Preview(name = "Game Level Previews", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun GameLevelBadgePreview() {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Implementation 1: Easy Level (Pastel Green Theme)
        GameLevelBadge(
            levelText = "Easy",
            icon = "🌱",
            backgroundColor = Color(0xFFE8F5E9),
            borderColor = Color(0xFF81C784),
            textColor = Color(0xFF2E7D32),
            onClick = {}
        )

        // Implementation 2: Medium Level (Playful Orange Theme)
        GameLevelBadge(
            levelText = "Medium",
            icon = "⚡",
            backgroundColor = Color(0xFFFFE0B2),
            borderColor = Color(0xFFFFB74D),
            textColor = Color(0xFFE65100),
            onClick = {}
        )

        // Implementation 3: Hard Level (Classic Golden Yellow Theme)
        GameLevelBadge(
            levelText = "Hard",
            icon = "⭐",
            onClick = {}
        )
    }
}

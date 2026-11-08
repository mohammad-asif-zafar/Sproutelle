package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.presentation.picturequest.components.PictureQuestEmojiMapper

/**
 * A highly adaptive, kid-friendly answer card designed for toddlers (3+ years old).
 * Scales dynamically across Mobile and Tablet form factors.
 * Natively attempts to render a full-surface live image layout first, automatically
 * deploying a vibrant standalone fallback emoji tile if image resources are unavailable or fail.
 */
@Composable
fun SingleImageAnswerCard(
    option: QuestionOption,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isTablet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Safety verification flag tracking runtime asset loading failures
    var isImageError by remember { mutableStateOf(false) }

    // Bouncy scale compression animation for little fingers when pushing the button
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "AnswerCardScale"
    )

    // Dynamic measurements matching target form factors
    val cardHeight = if (isTablet) 120.dp else 84.dp
    val emojiSize = if (isTablet) 58.sp else 42.sp
    val indicatorSize = if (isTablet) 36.dp else 26.dp
    val indicatorFontSize = if (isTablet) 18.sp else 14.sp

    // Comic-Sticker specific border and shadow metrics
    val shadowOffsetHeight = if (isTablet) 6.dp else 4.dp
    val shadowOffsetWidth = if (isTablet) 4.dp else 2.5.dp
    val strokeWidth = if (isSelected) (if (isTablet) 5.dp else 4.dp) else 2.5.dp
    val cornerRadius = if (isTablet) 28.dp else 22.dp

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFF4CAF50)  // Active Vibrant Green
            isSelected && isCorrect == false -> Color(0xFFE53935) // Active Comic Red
            else -> Color(0xFF1B365D)                             // High contrast navy border base
        },
        label = "CardBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFFE8F5E9)
            isSelected && isCorrect == false -> Color(0xFFFFEBEE)
            else -> Color.White
        },
        label = "CardContainerColor"
    )

    val hasValidImage = !option.imageUrl.isNullOrBlank() && !isImageError
    val pictureEmoji = PictureQuestEmojiMapper.getPictureEmoji(option.text)

    // Solid stylized cartoon shadow container wrapper
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight + shadowOffsetHeight)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        // Background toy-sticker solid offset drop shadow block
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = shadowOffsetHeight, start = shadowOffsetWidth)
                .background(Color(0xFFB0BEC5), shape = RoundedCornerShape(cornerRadius))
        )

        // Primary Foreground Card Layout Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                // Border sits directly on top of everything to perfectly encase edge-to-edge content layers
                .border(
                    width = strokeWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hasValidImage) {
                    // Production Mode: Renders dynamic network images at 100% full-surface grid scale
                    AsyncImage(
                        model = option.imageUrl,
                        contentDescription = option.text,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(cornerRadius)),
                        contentScale = ContentScale.Fit, // Preserves illustration aspect ratios precisely
                        onError = { isImageError = true } // Graceful internal interception path trigger
                    )

                    // Translucent overlay backing tint matching selection parameters
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(borderColor.copy(alpha = 0.12f))
                        )
                    }
                } else {
                    // Development/Fallback Mode: Delivers rich large toy emojis when URLs are absent or broken
                    Text(
                        text = pictureEmoji,
                        fontSize = emojiSize
                    )
                }

                // Top-right Pre-K checkmark / cross state overlay validation indicator tags
                if (isSelected) {
                    val indicatorPadding = if (isTablet) 8.dp else 6.dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(indicatorPadding)
                            .size(indicatorSize + 2.dp)
                    ) {
                        // Action Badge Drop Shadow
                        Box(
                            modifier = Modifier
                                .size(indicatorSize)
                                .align(Alignment.BottomStart)
                                .background(Color(0xFF37474F).copy(alpha = 0.3f), shape = CircleShape)
                        )

                        // Action Badge Foreground Disc Container
                        Box(
                            modifier = Modifier
                                .size(indicatorSize)
                                .align(Alignment.TopEnd)
                                .background(
                                    color = if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFE53935),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCorrect == true) "✓" else "✕",
                                color = Color.White,
                                fontSize = indicatorFontSize,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STUDIO PREVIEWS (MOBILE VS TABLET ADAPTIVE VERIFICATION)
// ============================================================================

@Preview(name = "Mobile - Default Unselected", widthDp = 180, showBackground = true)
@Composable
private fun MobileCardDefaultPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(
                    id = "1", questionId = "q1", text = "Pumpkin", isCorrect = false, sortOrder = 1
                ), isSelected = false, isCorrect = null, isTablet = false, onClick = {})
        }
    }
}

@Preview(name = "Mobile - Selected Incorrect State", widthDp = 180, showBackground = true)
@Composable
private fun MobileCardIncorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(
                    id = "2", questionId = "q1", text = "Chili", isCorrect = false, sortOrder = 2
                ), isSelected = true, isCorrect = false, isTablet = false, onClick = {})
        }
    }
}

@Preview(name = "Tablet - Selected Correct State", widthDp = 260, showBackground = true)
@Composable
private fun TabletCardCorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(
                    id = "3", questionId = "q1", text = "Apple", isCorrect = true, sortOrder = 3
                ), isSelected = true, isCorrect = true, isTablet = true, onClick = {})
        }
    }
}

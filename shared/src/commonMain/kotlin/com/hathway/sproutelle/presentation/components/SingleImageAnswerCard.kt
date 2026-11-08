package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * Scales dynamically to optimize interactive ergonomics across Mobile and Tablet targets.
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
    val cardHeight = if (isTablet) 110.dp else 78.dp
    val assetSize = if (isTablet) 76.dp else 54.dp
    val emojiSize = if (isTablet) 54.sp else 38.sp
    val indicatorSize = if (isTablet) 32.dp else 24.dp
    val indicatorFontSize = if (isTablet) 16.sp else 13.sp
    val strokeWidth = if (isSelected) (if (isTablet) 4.5.dp else 3.5.dp) else 1.dp
    val cornerRadius = if (isTablet) 28.dp else 22.dp

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFF4CAF50)
            isSelected && isCorrect == false -> Color(0xFFE53935)
            else -> Color(0xFFE0E0E0)
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

    val pictureEmoji = PictureQuestEmojiMapper.getPictureEmoji(option.text)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .border(
                width = strokeWidth,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom spring layer scales visually instead of native ripple
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 1.dp else 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!option.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = option.imageUrl,
                    contentDescription = option.text,
                    modifier = Modifier
                        .size(assetSize)
                        .clip(RoundedCornerShape(if (isTablet) 16.dp else 12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(pictureEmoji, fontSize = emojiSize)
            }

            // Top-right Pre-K checkmark / cross state overlay validation indicator tags
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (isTablet) 8.dp else 6.dp)
                        .size(indicatorSize)
                        .background(
                            color = if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFE53935),
                            shape = CircleShape
                        ),
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

// ============================================================================
// STUDIO PREVIEWS (MOBILE VS TABLET ADAPTIVE VERIFICATION)
// ============================================================================

@Preview(name = "Mobile - Default Unselected", widthDp = 180, showBackground = true)
@Composable
private fun MobileCardDefaultPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(id = "1", questionId = "q1", text = "Pumpkin", isCorrect = false, sortOrder = 1),
                isSelected = false,
                isCorrect = null,
                isTablet = false,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Mobile - Selected Incorrect State", widthDp = 180, showBackground = true)
@Composable
private fun MobileCardIncorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(id = "2", questionId = "q1", text = "Chili", isCorrect = false, sortOrder = 2),
                isSelected = true,
                isCorrect = false,
                isTablet = false,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Tablet - Selected Correct State", widthDp = 260, showBackground = true)
@Composable
private fun TabletCardCorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SingleImageAnswerCard(
                option = QuestionOption(id = "3", questionId = "q1", text = "Apple", isCorrect = true, sortOrder = 3),
                isSelected = true,
                isCorrect = true,
                isTablet = true,
                onClick = {}
            )
        }
    }
}

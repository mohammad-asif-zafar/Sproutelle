package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.presentation.picturequest.components.QuestionImageIllustration

/**
 * A refined compact Question Panel layout optimized for early learners (3+ years old).
 * Scales layout bounds dynamically to optimize interactive ergonomics across Mobile and Tablet targets.
 */
object PictureQuestQuestionCard {

    @Composable
    operator fun invoke(
        question: Question,
        isAudioMuted: Boolean,
        onAudioToggle: () -> Unit,
        isTablet: Boolean,
        modifier: Modifier = Modifier
    ) {
        val audioInteractionSource = remember { MutableInteractionSource() }
        val isAudioPressed by audioInteractionSource.collectIsPressedAsState()

        // Bouncy spring animation for the audio button interaction feedback loop
        val audioScale by animateFloatAsState(
            targetValue = if (isAudioPressed) 0.85f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "AudioButtonScale"
        )

        // Dynamic dimension metrics matching target devices
        val cardHeight = if (isTablet) 160.dp else 115.dp
        val containerSize = if (isTablet) 84.dp else 48.dp
        val illustrationSize = if (isTablet) 64.dp else 40.dp
        val illustrationFontSize = if (isTablet) 42.sp else 28.sp
        val audioDiscSize = if (isTablet) 56.dp else 40.dp
        val audioEmojiSize = if (isTablet) 24.sp else 18.sp
        val subTitleFontSize = if (isTablet) 16.sp else 12.sp
        val primaryTitleFontSize = if (isTablet) 28.sp else 18.sp
        val horizontalGap = if (isTablet) 16.dp else 10.dp
        val cornerRadius = if (isTablet) 28.dp else 24.dp

        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: Image / Emoji Context Box Container
                    Box(
                        modifier = Modifier
                            .size(containerSize)
                            .background(Color(0xFFE1F5FE), shape = RoundedCornerShape(if (isTablet) 18.dp else 14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        QuestionImageIllustration(
                            imageUrl = question.imageUrl,
                            text = question.text,
                            assetSize = illustrationSize,
                            fallbackFontSize = illustrationFontSize
                        )
                    }

                    Spacer(modifier = Modifier.width(horizontalGap))

                    // Center: Prompt and Target Text Information Stack
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Which one is a",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0288D1),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = subTitleFontSize
                        )

                        Text(
                            text = question.text.uppercase(),
                            style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0D47A1),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            fontSize = primaryTitleFontSize
                        )
                    }

                    Spacer(modifier = Modifier.width(horizontalGap))

                    // Right Side: Tactile Audio Toggle Button Controller Disc
                    Box(
                        modifier = Modifier
                            .size(audioDiscSize)
                            .graphicsLayer {
                                scaleX = audioScale
                                scaleY = audioScale
                            }
                            .background(
                                color = if (isAudioMuted) Color(0xFFFFCC80) else Color(0xFF29B6F6),
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = audioInteractionSource,
                                indication = null, // Handled natively by spring modifiers
                                onClick = onAudioToggle
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAudioMuted) "🔇" else "🔊",
                            fontSize = audioEmojiSize,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// MULTIPLE PREVIEWS
// ==========================================

@Preview(
    name = "Mobile Layout View - Audio Active", showBackground = true, backgroundColor = 0xFFE0F7FA
)
@Composable
private fun QuestionCardMobileActivePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "pq_q1",
                    gameId = "picture_quest",
                    subcategoryId = "sub_1",
                    text = "Mushroom",
                    type = "image_selection",
                    instructions = "Look closely.",
                    sortOrder = 1,
                    isDraft = false,
                    options = emptyList()
                ), isAudioMuted = false, onAudioToggle = {}, isTablet = false
            )
        }
    }
}

@Preview(
    name = "Tablet Layout View - Audio Muted", showBackground = true, backgroundColor = 0xFFE0F7FA
)
@Composable
private fun QuestionCardTabletMutedPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "pq_q2",
                    gameId = "picture_quest",
                    subcategoryId = "sub_1",
                    text = "Watermelon",
                    type = "image_selection",
                    instructions = "Look closely.",
                    sortOrder = 2,
                    isDraft = false,
                    options = emptyList()
                ), isAudioMuted = true, onAudioToggle = {}, isTablet = true
            )
        }
    }
}
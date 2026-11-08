package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.presentation.picturequest.components.QuestionImageIllustration

/**
 * A highly interactive, comic-styled Question Panel layout optimized for toddlers (Ages 3+).
 * Positions the Audio Control Button below the main text to maximize horizontal layout budget
 * for long question text variables.
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
        // Validation check flag: Determines if the data payload lacks image assets completely
        val hasNoImage = question.imageUrl.isNullOrBlank() && question.imageAssetId.isNullOrBlank()

        // Continuous Kid-Friendly Idle Floating Wobble Loop Animation
        val infiniteTransition = rememberInfiniteTransition(label = "CardWobbleLoop")
        val continuousRotation by infiniteTransition.animateFloat(
            initialValue = -1.5f, targetValue = 1.5f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "WobbleRotation"
        )
        val continuousTranslationY by infiniteTransition.animateFloat(
            initialValue = -3f, targetValue = 3f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = SineGradientEaser),
                repeatMode = RepeatMode.Reverse
            ), label = "FloatingTranslation"
        )

        // Kid-centric dynamic spacing metrics - shifts heights slightly depending on image presence
        val cardHeight =
            if (isTablet) (if (hasNoImage) 155.dp else 210.dp) else (if (hasNoImage) 120.dp else 165.dp)
        val strokeWidth = if (isTablet) 4.dp else 3.dp
        val outerCornerRadius = if (isTablet) 32.dp else 24.dp

        val illustrationContainerSize = if (isTablet) 96.dp else 64.dp
        val illustrationSize = if (isTablet) 72.dp else 48.dp
        val illustrationFontSize = if (isTablet) 48.sp else 32.sp

        val audioDiscSize = if (isTablet) 52.dp else 42.dp
        val bubbleHorizontalPadding = if (isTablet) 16.dp else 12.dp
        val bubbleVerticalPadding = if (isTablet) 6.dp else 4.dp
        val subTitleFontSize = if (isTablet) 15.sp else 11.sp
        val horizontalGap = if (isTablet) 20.dp else 12.dp

        // Dynamic Responsive Font Size Strategy to maximize long text layouts
        val textLength = question.text.length
        val primaryTitleFontSize = when {
            isTablet && textLength > 12 -> 24.sp
            isTablet && textLength > 8 -> 28.sp
            isTablet -> 34.sp
            !isTablet && textLength > 12 -> 16.sp
            !isTablet && textLength > 8 -> 19.sp
            else -> 22.sp
        }

        Box(
            modifier = modifier.fillMaxWidth().height(cardHeight + (if (isTablet) 6.dp else 4.dp))
                .graphicsLayer {
                    rotationZ = continuousRotation
                    translationY = continuousTranslationY
                }) {
            // Background toy-sticker solid offset drop shadow block
            Box(
                modifier = Modifier.matchParentSize().padding(
                    top = if (isTablet) 6.dp else 4.dp, start = if (isTablet) 4.dp else 3.dp
                ).background(Color(0xFFB0BEC5), shape = RoundedCornerShape(outerCornerRadius))
            )

            // Primary Foreground Interactive Card Sheet
            Card(
                modifier = Modifier.fillMaxWidth().height(cardHeight).border(
                    width = strokeWidth,
                    color = Color(0xFF1B365D),
                    shape = RoundedCornerShape(outerCornerRadius)
                ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(outerCornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = if (isTablet) 18.dp else 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (hasNoImage) Arrangement.Center else Arrangement.Start
                ) {
                    // Left Node: Saturated Geometric Illustration Frame - Rendered only if an image is present
                    if (!hasNoImage) {
                        Surface(
                            modifier = Modifier.size(illustrationContainerSize).border(
                                width = if (isTablet) 3.5.dp else 2.5.dp,
                                color = Color(0xFF0288D1),
                                shape = RoundedCornerShape(if (isTablet) 22.dp else 16.dp)
                            ),
                            shape = RoundedCornerShape(if (isTablet) 22.dp else 16.dp),
                            color = Color(0xFFE1F5FE)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                QuestionImageIllustration(
                                    imageUrl = question.imageUrl,
                                    text = question.text,
                                    assetSize = illustrationSize,
                                    fallbackFontSize = illustrationFontSize
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(horizontalGap))
                    }

                    // Center & Bottom Node: Central Vertically Integrated Text + Action Button Column Stack
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Prompt Phrase Speech Bubble
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF9C4),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "WHICH ONE IS A",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = subTitleFontSize,
                                    letterSpacing = 0.8.sp,
                                    lineHeight = subTitleFontSize
                                ),
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                modifier = Modifier.padding(
                                    horizontal = bubbleHorizontalPadding,
                                    vertical = bubbleVerticalPadding
                                )
                            )
                        }

                        // Saturated Ultra-Bold Vocabulary Target Identifier
                        Text(
                            text = question.text.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = primaryTitleFontSize,
                                lineHeight = (primaryTitleFontSize.value + 2f).sp
                            ),
                            color = Color(0xFF0D47A1),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        SproutelleAudioButton(
                            isMuted = isAudioMuted,
                            isPlaying = true, // Set to true for layout confirmation preview state matching
                            buttonSize = audioDiscSize,
                            contentPadding = PaddingValues(0.dp),
                            onClick = onAudioToggle
                        )
                    }
                }
            }
        }
    }
}

private val SineGradientEaser = Easing { fraction ->
    androidx.compose.ui.util.lerp(0f, 1f, kotlin.math.sin(fraction * kotlin.math.PI.toFloat()))
}

// ============================================================================
// 📱 MOBILE STATE PREVIEWS (IMAGE PRESENT VS MISSING)
// ============================================================================
@Preview(name = "Mobile - Image Is There 🍏", widthDp = 360, showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun MobileLayoutWithImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "m_img", gameId = "g1", subcategoryId = "s1", text = "Apple",
                    type = "image_selection", instructions = null, sortOrder = 1, isDraft = false,
                    imageUrl = "has_valid_url_path"
                ),
                isAudioMuted = false,
                onAudioToggle = {},
                isTablet = false
            )
        }
    }
}
@Preview(name = "Mobile - Image Is Missing ❌", widthDp = 360, showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun MobileLayoutNoImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "m_no_img", gameId = "g1", subcategoryId = "s1", text = "Mushroom",
                    type = "image_selection", instructions = null, sortOrder = 2, isDraft = false,
                    imageUrl = null, imageAssetId = null
                ),
                isAudioMuted = false,
                onAudioToggle = {},
                isTablet = false
            )
        }
    }
}
// ============================================================================
// 📑 TABLET STATE PREVIEWS (IMAGE PRESENT VS MISSING)
// ============================================================================
@Preview(name = "Tablet - Image Is There 🍉", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun TabletLayoutWithImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp).width(450.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "t_img", gameId = "g1", subcategoryId = "s1", text = "Watermelon",
                    type = "image_selection", instructions = null, sortOrder = 3, isDraft = false,
                    imageUrl = "has_valid_url_path"
                ),
                isAudioMuted = true,
                onAudioToggle = {},
                isTablet = true
            )
        }
    }
}
@Preview(name = "Tablet - Image Is Missing ❌", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun TabletLayoutNoImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp).width(450.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "t_no_img", gameId = "g1", subcategoryId = "s1", text = "Pumpkin",
                    type = "image_selection", instructions = null, sortOrder = 4, isDraft = false,
                    imageUrl = null, imageAssetId = null
                ),
                isAudioMuted = false,
                onAudioToggle = {},
                isTablet = true
            )
        }
    }
}
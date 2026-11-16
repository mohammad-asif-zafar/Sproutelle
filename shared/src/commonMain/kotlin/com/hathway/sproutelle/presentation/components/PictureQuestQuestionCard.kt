package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.presentation.picturequest.components.QuestionImageIllustration

/**
 * A highly interactive, comic-styled Question Panel layout optimized for toddlers (Ages 3+).
 * Dynamically adapts according to backend-configured question types and instructions.
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
        val activeIllustrationUrl = question.questionIllustrationUrl ?: question.imageUrl
        val hasNoImage = activeIllustrationUrl.isNullOrBlank() && question.imageAssetId.isNullOrBlank() && question.questionIllustrationAssetId.isNullOrBlank()

        // Kid-Friendly Interactive "Little Shake" Animation Loop
        val infiniteTransition = rememberInfiniteTransition(label = "CardShakeLoop")
        val shakeRotation by infiniteTransition.animateFloat(
            initialValue = -2.2f,
            targetValue = 2.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShakeRotation"
        )
        val shakeTranslationX by infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShakeTranslationX"
        )
        val shakeTranslationY by infiniteTransition.animateFloat(
            initialValue = -2.0f,
            targetValue = 2.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = SineGradientEaser),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShakeTranslationY"
        )

        val cardHeight = if (isTablet) (if (hasNoImage) 155.dp else 200.dp) else (if (hasNoImage) 110.dp else 125.dp)

        val illustrationContainerSize = if (isTablet) 90.dp else 54.dp
        val illustrationSize = if (isTablet) 68.dp else 40.dp
        val illustrationFontSize = if (isTablet) 48.sp else 28.sp

        val audioDiscSize = if (isTablet) 52.dp else 42.dp
        val bubbleHorizontalPadding = if (isTablet) 16.dp else 12.dp
        val bubbleVerticalPadding = if (isTablet) 6.dp else 4.dp
        val subTitleFontSize = if (isTablet) 15.sp else 11.sp
        val horizontalGap = if (isTablet) 20.dp else 12.dp

        val textLength = question.text.length
        val primaryTitleFontSize = when {
            isTablet && textLength > 12 -> 24.sp
            isTablet && textLength > 8 -> 28.sp
            isTablet -> 34.sp
            !isTablet && textLength > 12 -> 16.sp
            !isTablet && textLength > 8 -> 19.sp
            else -> 22.sp
        }

        val bannerShape = remember { CurvedBannerShape(cornerRadiusRatio = 0.35f, topArchFactor = 0.08f, bottomArchFactor = 0.03f) }

        // Dynamic prompt text from backend instructions or format defaults
        val promptBubbleText = when {
            !question.instructions.isNullOrBlank() -> question.instructions.uppercase()
            question.type.contains("identify", ignoreCase = true) -> "WHAT DO YOU SEE?"
            question.type.contains("different", ignoreCase = true) -> "WHICH ONE DOES NOT BELONG?"
            question.type.contains("category", ignoreCase = true) -> "FIND THE MATCH"
            question.type.contains("match", ignoreCase = true) -> "MATCH PICTURE & WORD"
            else -> "WHICH ONE IS A"
        }

        Box(
            modifier = modifier
                .graphicsLayer {
                    rotationZ = shakeRotation
                    translationX = shakeTranslationX
                    translationY = shakeTranslationY
                }
                .padding(
                    top = if (isTablet) 6.dp else 4.dp,
                    start = if (isTablet) 2.dp else 1.dp,
                    end = if (isTablet) 2.dp else 1.dp
                )
                .background(Color(0xFFB0BEC5).copy(alpha = 0.6f), shape = bannerShape)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = bannerShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isTablet) 18.dp else 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (hasNoImage) Arrangement.Center else Arrangement.Start
                ) {
                    if (!hasNoImage) {
                        Surface(
                            modifier = Modifier
                                .size(illustrationContainerSize)
                                .border(
                                    width = if (isTablet) 3.5.dp else 2.5.dp,
                                    color = Color(0xFF0288D1),
                                    shape = RoundedCornerShape(if (isTablet) 22.dp else 16.dp)
                                ),
                            shape = RoundedCornerShape(if (isTablet) 22.dp else 16.dp),
                            color = Color(0xFFE1F5FE)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                QuestionImageIllustration(
                                    imageUrl = activeIllustrationUrl,
                                    text = question.text,
                                    assetSize = illustrationSize,
                                    fallbackFontSize = illustrationFontSize
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(horizontalGap))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF9C4),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = promptBubbleText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = subTitleFontSize,
                                    letterSpacing = 0.8.sp,
                                    lineHeight = subTitleFontSize
                                ),
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(
                                    horizontal = bubbleHorizontalPadding,
                                    vertical = bubbleVerticalPadding
                                )
                            )
                        }

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
                            isPlaying = true,
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

class CurvedBannerShape(
    private val cornerRadiusRatio: Float = 0.25f,
    private val topArchFactor: Float = 0.08f,
    private val bottomArchFactor: Float = 0.04f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        val r = (minOf(width, height) * cornerRadiusRatio).coerceAtMost(height / 2f)
        val topDip = height * topArchFactor
        val bottomDip = height * bottomArchFactor

        val path = Path().apply {
            moveTo(r, topDip)

            quadraticBezierTo(
                x1 = width / 2f, y1 = -topDip,
                x2 = width - r, y2 = topDip
            )

            quadraticBezierTo(
                x1 = width, y1 = topDip,
                x2 = width, y2 = topDip + r
            )

            lineTo(width, height - bottomDip - r)

            quadraticBezierTo(
                x1 = width, y1 = height - bottomDip,
                x2 = width - r, y2 = height - bottomDip
            )

            quadraticBezierTo(
                x1 = width / 2f, y1 = height + bottomDip,
                x2 = r, y2 = height - bottomDip
            )

            quadraticBezierTo(
                x1 = 0f, y1 = height - bottomDip,
                x2 = 0f, y2 = height - bottomDip - r
            )

            lineTo(0f, topDip + r)

            quadraticBezierTo(
                x1 = 0f, y1 = topDip,
                x2 = r, y2 = topDip
            )

            close()
        }

        return Outline.Generic(path)
    }
}

@Preview(name = "Mobile - Image Present", widthDp = 360, showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun MobileLayoutWithImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "m_img", gameId = "g1", subcategoryId = "s1", text = "Apple",
                    type = "find_correct_picture", instructions = "WHICH ONE IS AN APPLE?", sortOrder = 1, isDraft = false,
                    imageUrl = "has_valid_url_path"
                ),
                isAudioMuted = false,
                onAudioToggle = {},
                isTablet = false
            )
        }
    }
}

@Preview(name = "Tablet - Image Present", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun TabletLayoutWithImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp).width(450.dp)) {
            PictureQuestQuestionCard(
                question = Question(
                    id = "t_img", gameId = "g1", subcategoryId = "s1", text = "Watermelon",
                    type = "identify_object", instructions = "WHAT DO YOU SEE?", sortOrder = 3, isDraft = false,
                    imageUrl = "has_valid_url_path"
                ),
                isAudioMuted = true,
                onAudioToggle = {},
                isTablet = true
            )
        }
    }
}

package com.hathway.sproutelle.presentation.wordbuilder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun WordMascotPromptCard(
    promptText: String,
    categoryEmoji: String,
    imageUrl: String?,
    contentDescription: String,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    speechBubbleIcon: String = "💬"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MascotIdleLoop")
    val mascotFloatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MascotFloatY"
    )
    val bubblePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BubblePulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isTablet) 3.5.dp else 2.5.dp,
                color = Color(0xFFB2EBF2),
                shape = RoundedCornerShape(if (isTablet) 36.dp else 28.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(if (isTablet) 36.dp else 28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isTablet) 24.dp else 16.dp,
                    vertical = if (isTablet) 20.dp else 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
        ) {
            // Mascot Circular Disc Frame with floating animation & speech bubble
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.graphicsLayer { translationY = mascotFloatY }
            ) {
                Surface(
                    modifier = Modifier.size(if (isTablet) 120.dp else 90.dp),
                    shape = CircleShape,
                    color = Color(0xFFE1F5FE),
                    border = BorderStroke(2.dp, Color(0xFF81D4FA))
                ) {
                    AnimatedContent(
                        targetState = Pair(imageUrl, categoryEmoji),
                        transitionSpec = {
                            (fadeIn(tween(250)) + scaleIn(initialScale = 0.85f)) togetherWith fadeOut(tween(200))
                        },
                        label = "MascotVisualTransition"
                    ) { (url, emoji) ->
                        Box(contentAlignment = Alignment.Center) {
                            if (!url.isNullOrBlank()) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = contentDescription,
                                    modifier = Modifier
                                        .size(if (isTablet) 90.dp else 66.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = emoji,
                                    fontSize = if (isTablet) 64.sp else 46.sp
                                )
                            }
                        }
                    }
                }

                // Speech Bubble Indicator
                Surface(
                    modifier = Modifier
                        .size(if (isTablet) 36.dp else 28.dp)
                        .offset(x = 6.dp, y = 4.dp)
                        .graphicsLayer {
                            scaleX = bubblePulse
                            scaleY = bubblePulse
                        },
                    shape = CircleShape,
                    color = Color(0xFF76FF03),
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = speechBubbleIcon,
                            fontSize = if (isTablet) 16.sp else 12.sp
                        )
                    }
                }
            }

            // Pink-Bordered Question Prompt Speech Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF1F5),
                border = BorderStroke(
                    width = if (isTablet) 3.dp else 2.dp,
                    color = Color(0xFFFF4081)
                ),
                shadowElevation = 2.dp
            ) {
                AnimatedContent(
                    targetState = promptText,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "PromptTextTransition"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isTablet) 20.sp else 16.sp,
                            lineHeight = if (isTablet) 24.sp else 20.sp
                        ),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Mascot Card - Mobile", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun WordMascotPromptCardMobilePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WordMascotPromptCard(
                promptText = "What do we call this Animal?",
                categoryEmoji = "🦊",
                imageUrl = null,
                contentDescription = "Fox",
                isTablet = false
            )
        }
    }
}

package com.hathway.sproutelle.presentation.numberquest.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay

@Composable
fun NumberCountGallery(
    questionKey: String,
    promptText: String,
    targetCount: Int,
    itemEmoji: String,
    imageUrl: String?,
    isTablet: Boolean,
    onCountItemTap: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Tracks which item indices the child has tapped while counting (resets per question)
    val countedOrder = remember(questionKey, targetCount) { mutableStateListOf<Int>() }

    val infiniteTransition = rememberInfiniteTransition(label = "CountGalleryIdle")
    val gentleSway by infiniteTransition.animateFloat(
        initialValue = -2.5f, targetValue = 2.5f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ItemSway"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isTablet) 32.dp else 26.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = BorderStroke(
            width = if (isTablet) 3.5.dp else 2.5.dp, color = Color(0xFFB2EBF2)
        ),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = if (isTablet) 20.dp else 16.dp,
                vertical = if (isTablet) 18.dp else 14.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
        ) {
            // Prompt Question Header
            Text(
                text = promptText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (isTablet) 24.sp else 19.sp
                ),
                color = Color(0xFF1B365D),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            // Countable Items Gallery Display
            val itemSize = if (isTablet) 68.sp else 52.sp
            val itemsPerRow = if (targetCount > 4) 4 else if (targetCount > 2) 3 else 2
            val indexedItems = List(targetCount) { it }
            val chunkedCountables = indexedItems.chunked(itemsPerRow)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 6.dp)
            ) {
                chunkedCountables.forEach { rowIndices ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowIndices.forEach { itemIndex ->
                            val entranceScale =
                                remember(questionKey, itemIndex) { Animatable(0.4f) }
                            LaunchedEffect(questionKey, itemIndex) {
                                delay(itemIndex * 75L)
                                entranceScale.animateTo(
                                    targetValue = 1f, animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }

                            val countNumber = countedOrder.indexOf(itemIndex)
                                .let { if (it >= 0) it + 1 else null }
                            val isCounted = countNumber != null

                            val tapBounceScale by animateFloatAsState(
                                targetValue = if (isCounted) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "CountTapBounce"
                            )

                            Box(
                                contentAlignment = Alignment.TopEnd,
                                modifier = Modifier.graphicsLayer {
                                    val combined = entranceScale.value * tapBounceScale
                                    scaleX = combined
                                    scaleY = combined
                                    rotationZ = if (itemIndex % 2 == 0) gentleSway else -gentleSway
                                }.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!isCounted) {
                                        countedOrder.add(itemIndex)
                                        onCountItemTap(countedOrder.size)
                                    } else {
                                        onCountItemTap(countNumber!!)
                                    }
                                }) {
                                if (!imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Count Item ${itemIndex + 1}",
                                        modifier = Modifier.size(if (isTablet) 64.dp else 48.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = itemEmoji, fontSize = itemSize
                                    )
                                }

                                // Numbered counting bubble appears when tapped by child

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = countNumber != null, enter = scaleIn(
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    ), exit = scaleOut()
                                ) {
                                    Box(
                                        modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                            .size(if (isTablet) 26.dp else 20.dp)
                                            .background(Color(0xFF0288D1), CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (countNumber ?: 1).toString(),
                                            color = Color.White,
                                            fontSize = if (isTablet) 13.sp else 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Count Gallery - Lions", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun NumberCountGalleryPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberCountGallery(
                questionKey = "nq_q1",
                promptText = "Count the lion?",
                targetCount = 3,
                itemEmoji = "🦁",
                imageUrl = null,
                isTablet = false
            )
        }
    }
}
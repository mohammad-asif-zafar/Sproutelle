package com.hathway.sproutelle.presentation.wordbuilder.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WordTargetSlots(
    placedLetters: List<Char?>,
    isCorrect: Boolean?,
    isTablet: Boolean,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val slotSize: Dp = if (isTablet) 68.dp else 52.dp

    // Horizontal shake animation when the full word is wrong
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isCorrect) {
        if (isCorrect == false) {
            val offsets = listOf(-12f, 12f, -9f, 9f, -5f, 5f, 0f)
            offsets.forEach { target ->
                shakeOffset.animateTo(target, animationSpec = tween(45))
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset.value },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        placedLetters.forEachIndexed { index, letter ->
            val isFilled = letter != null
            val isSlotCorrect = isCorrect == true
            val isSlotIncorrect = isCorrect == false

            // Spring bounce whenever a letter is placed into this slot
            val popScale by animateFloatAsState(
                targetValue = if (isFilled) 1.0f else 0.94f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "SlotPopScale"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    isSlotCorrect -> Color(0xFF4CAF50)
                    isSlotIncorrect -> Color(0xFFE53935)
                    isFilled -> Color(0xFF0288D1)
                    else -> Color(0xFF76FF03)
                },
                label = "SlotBorderColor"
            )

            val backgroundColor by animateColorAsState(
                targetValue = when {
                    isSlotCorrect -> Color(0xFFE8F5E9)
                    isSlotIncorrect -> Color(0xFFFFEBEE)
                    else -> Color.White
                },
                label = "SlotBackgroundColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = if (isTablet) 6.dp else 4.dp)
                    .size(slotSize)
                    .graphicsLayer {
                        scaleX = popScale
                        scaleY = popScale
                    }
                    .clickable(enabled = isFilled) { onSlotClick(index) }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
                    color = backgroundColor,
                    border = BorderStroke(
                        width = if (isFilled) 3.dp else 2.5.dp,
                        color = borderColor
                    ),
                    shadowElevation = if (isFilled) 4.dp else 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (letter != null) {
                            Text(
                                text = letter.uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = if (isTablet) 32.sp else 24.sp
                                ),
                                color = Color(0xFF0D47A1),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                if (isFilled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 6.dp)
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .background(
                                color = if (isSlotCorrect) Color(0xFF4CAF50) else Color(0xFF00C853),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

@Preview(name = "Slots - In Progress", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun WordTargetSlotsPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WordTargetSlots(
                placedLetters = listOf('F', 'O', null),
                isCorrect = null,
                isTablet = false,
                onSlotClick = {}
            )
        }
    }
}

@Preview(name = "Slots - Correct", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun WordTargetSlotsCorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WordTargetSlots(
                placedLetters = listOf('F', 'O', 'X'),
                isCorrect = true,
                isTablet = false,
                onSlotClick = {}
            )
        }
    }
}

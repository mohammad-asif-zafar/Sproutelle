package com.hathway.sproutelle.presentation.numberquest.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.hathway.sproutelle.presentation.numberquest.NumberChoice
import kotlinx.coroutines.delay

@Composable
fun NumberChoiceItem(
    choice: NumberChoice,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isTablet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    entranceIndex: Int = 0,
    enabled: Boolean = true
) {
    // Staggered spring entrance per question
    val entranceScale = remember(choice.id) { Animatable(0.55f) }
    LaunchedEffect(choice.id) {
        delay(entranceIndex * 55L)
        entranceScale.animateTo(
            targetValue = 1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // Horizontal wobble shake when this choice is wrong
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isSelected, isCorrect) {
        if (isSelected && isCorrect == false) {
            listOf(-10f, 10f, -8f, 8f, -4f, 4f, 0f).forEach { target ->
                shakeOffset.animateTo(target, animationSpec = tween(40))
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val stateScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.90f
            isSelected && isCorrect == true -> 1.08f
            else -> 1.0f
        }, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "NumberChoiceScale"
    )

    val tileSize: Dp = if (isTablet) 110.dp else 84.dp
    val fontSize = if (isTablet) 52.sp else 38.sp
    val cornerRadius = if (isTablet) 28.dp else 22.dp

    val baseColor = Color(choice.colorHex)
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFF4CAF50)
            isSelected && isCorrect == false -> Color(0xFFE53935)
            else -> Color.White.copy(alpha = 0.9f)
        }, label = "TileBorderColor"
    )

    Box(
        modifier = modifier.size(tileSize).graphicsLayer {
            val combined = entranceScale.value * stateScale
            scaleX = combined
            scaleY = combined
            translationX = shakeOffset.value
        }.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        // 3D Shadow Plate Rim
        Box(
            modifier = Modifier.matchParentSize()
                .offset(y = if (isTablet) 6.dp else 4.dp, x = if (isTablet) 3.dp else 2.dp)
                .background(
                    color = Color(0xFF37474F).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        )

        // Foreground 3D Tactile Tile
        Card(
            modifier = Modifier.fillMaxSize().border(
                width = if (isSelected) 4.dp else 2.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
            colors = CardDefaults.cardColors(containerColor = baseColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = choice.number.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = fontSize),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )

                if (isSelected) {
                    val badgeSize = if (isTablet) 32.dp else 24.dp
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(badgeSize)
                            .background(
                                color = if (isCorrect == true) Color(0xFF4CAF50) else Color(
                                    0xFFE53935
                                ), shape = CircleShape
                            ).border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCorrect == true) "✓" else "✕",
                            color = Color.White,
                            fontSize = if (isTablet) 16.sp else 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Number Choice Grid", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun NumberChoiceTilesPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberChoiceTiles(
                choices = listOf(
                    NumberChoice("c1", 4, 0xFFAB47BC, false),
                    NumberChoice("c2", 3, 0xFFFF9800, true),
                    NumberChoice("c3", 2, 0xFF4CAF50, false),
                    NumberChoice("c4", 1, 0xFFFFCA28, false)
                ), selectedChoiceId = "c2", isCorrect = true, isTablet = false, onChoiceClick = {})
        }
    }
}
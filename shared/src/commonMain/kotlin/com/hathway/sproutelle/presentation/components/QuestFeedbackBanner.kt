package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object QuestFeedbackBanner {

    @Composable
    operator fun invoke(
        isCorrect: Boolean,
        modifier: Modifier = Modifier
    ) {
        val scaleAnim = remember { Animatable(0.85f) }

        LaunchedEffect(isCorrect) {
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            shape = RoundedCornerShape(20.dp),
            color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isCorrect) "🦊" else "🤔",
                    fontSize = 32.sp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
                    ) {
                        Text(
                            text = if (isCorrect) "Amazing! ⭐" else "Try Again!",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isCorrect) "You found the right picture!" else "That's not correct. Look again and choose the right picture!",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ============================================================================
// MULTI-STATE PREVIEWS (MOBILE & TABLET CONSTRAINTS)
// ============================================================================

@Preview(name = "Mobile - Success State", device = "spec:width=411dp,height=120dp", showBackground = true)
@Composable
private fun MobileFeedbackSuccessPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestFeedbackBanner(isCorrect = true)
        }
    }
}

@Preview(name = "Mobile - Incorrect State", device = "spec:width=411dp,height=120dp", showBackground = true)
@Composable
private fun MobileFeedbackErrorPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestFeedbackBanner(isCorrect = false)
        }
    }
}

@Preview(name = "Tablet - Success State", device = "spec:width=1280dp,height=140dp,orientation=landscape", showBackground = true)
@Composable
private fun TabletFeedbackSuccessPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            QuestFeedbackBanner(isCorrect = true)
        }
    }
}

@Preview(name = "Tablet - Incorrect State", device = "spec:width=1280dp,height=140dp,orientation=landscape", showBackground = true)
@Composable
private fun TabletFeedbackErrorPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            QuestFeedbackBanner(isCorrect = false)
        }
    }
}
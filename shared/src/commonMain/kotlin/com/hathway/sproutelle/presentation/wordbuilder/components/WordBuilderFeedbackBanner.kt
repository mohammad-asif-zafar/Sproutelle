package com.hathway.sproutelle.presentation.wordbuilder.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun WordBuilderFeedbackBanner(
    isCorrect: Boolean,
    targetWord: String,
    modifier: Modifier = Modifier,
    successBadgeText: String = "Great Job! ⭐",
    retryBadgeText: String = "Try Again!",
    successMessage: String = "You spelled ${targetWord.uppercase()} correctly!",
    retryMessage: String = "That's not correct. Tap a letter to remove and try again!"
) {
    val scaleAnim = remember { Animatable(0.8f) }

    LaunchedEffect(isCorrect) {
        scaleAnim.snapTo(0.8f)
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
                text = if (isCorrect) "🎉" else "🤔",
                fontSize = 32.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
                ) {
                    Text(
                        text = if (isCorrect) successBadgeText else retryBadgeText,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isCorrect) successMessage else retryMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(name = "Banner - Correct", showBackground = true)
@Composable
private fun FeedbackBannerCorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WordBuilderFeedbackBanner(isCorrect = true, targetWord = "FOX")
        }
    }
}

@Preview(name = "Banner - Incorrect", showBackground = true)
@Composable
private fun FeedbackBannerIncorrectPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WordBuilderFeedbackBanner(isCorrect = false, targetWord = "FOX")
        }
    }
}

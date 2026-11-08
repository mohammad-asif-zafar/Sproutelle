package com.hathway.sproutelle.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.fox_great_job
import sproutelle.shared.generated.resources.fox_try_again

/**
 * A highly visual graphic feedback banner optimized for children (3+ years old).
 * Replaces plain text strings with rich cartoon assets featuring spring entrance animations.
 */
object QuestFeedbackBannerImage {

    @Composable
    operator fun invoke(
        isCorrect: Boolean,
        modifier: Modifier = Modifier
    ) {
        val scaleAnim = remember { Animatable(0.6f) }

        // Bouncy entry pop physics designed to capture attention and reward progress
        LaunchedEffect(isCorrect) {
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Select correct vector drawable mapping the active game result path context
        val graphicResource = if (isCorrect) Res.drawable.fox_great_job else Res.drawable.fox_try_again
        val graphicDescription = if (isCorrect) "Great Job Mascot Illustration" else "Try Again Mascot Illustration"

        Box(
            modifier = modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(graphicResource),
                contentDescription = graphicDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ============================================================================
// MULTI-STATE ADAPTIVE PREVIEWS (MOBILE & TABLET CONSTRAINTS)
// ============================================================================

@Preview(name = "Mobile - Great Job State", device = "spec:width=411dp,height=220dp", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun MobileFeedbackSuccessPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestFeedbackBannerImage(isCorrect = true)
        }
    }
}

@Preview(name = "Mobile - Try Again State", device = "spec:width=411dp,height=220dp", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun MobileFeedbackErrorPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestFeedbackBannerImage(isCorrect = false)
        }
    }
}

@Preview(name = "Tablet - Great Job State", device = "spec:width=1280dp,height=300dp,orientation=landscape", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun TabletFeedbackSuccessPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            QuestFeedbackBannerImage(isCorrect = true)
        }
    }
}

@Preview(name = "Tablet - Try Again State", device = "spec:width=1280dp,height=300dp,orientation=landscape", showBackground = true, backgroundColor = 0xFFE0F7FA)
@Composable
private fun TabletFeedbackErrorPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            QuestFeedbackBanner(isCorrect = false)
        }
    }
}

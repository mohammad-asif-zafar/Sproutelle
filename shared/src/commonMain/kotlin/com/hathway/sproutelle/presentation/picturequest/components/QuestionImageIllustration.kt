package com.hathway.sproutelle.presentation.picturequest.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/**
 * A highly adaptive target graphic illustration slot designed for toddler viewports (Ages 3+).
 * Gracefully switches between dynamic asset URLs and playful, color-rich placeholder emojis.
 */
object QuestionImageIllustration {

    @Composable
    operator fun invoke(
        imageUrl: String?,
        text: String,
        modifier: Modifier = Modifier,
        assetSize: androidx.compose.ui.unit.Dp = 40.dp,
        fallbackFontSize: androidx.compose.ui.unit.TextUnit = 28.sp
    ) {
        var isError by remember { mutableStateOf(false) }

        Box(
            modifier = modifier.size(assetSize),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank() && !isError) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Question Target Visual Graphic",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    onError = { isError = true }
                )
            }

            if (imageUrl.isNullOrBlank() || isError) {
                Text(
                    text = PictureQuestEmojiMapper.getPictureEmoji(text),
                    fontSize = fallbackFontSize,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// MULTIPLE PREVIEWS
// ==========================================

@Preview(name = "Illustration - Emoji Fallback Mode", showBackground = true)
@Composable
private fun IllustrationEmojiFallbackPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuestionImageIllustration(
                imageUrl = null,
                text = "Mushroom"
            )
        }
    }
}

@Preview(name = "Illustration - Network Failure Fallback Mode", showBackground = true)
@Composable
private fun IllustrationNetworkErrorPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            // Emulates an invalid network layout pipeline triggering the fallback state path
            QuestionImageIllustration(
                imageUrl = "https://invalid.url",
                text = "Watermelon"
            )
        }
    }
}

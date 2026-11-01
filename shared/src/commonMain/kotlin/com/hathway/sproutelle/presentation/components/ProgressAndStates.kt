package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun SproutelleProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(SproutelleDimens.ProgressBarHeight)
            .clip(RoundedCornerShape(5.dp)),
        color = SproutelleColors.LeafGreen,
        trackColor = SproutelleColors.Mint
    )
}

@Composable
fun LoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .padding(horizontal = SproutelleSpacing.XLarge)
                .widthIn(max = 260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SproutelleDimens.MobileContentPadding,
                        vertical = SproutelleSpacing.Large
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
            ) {
                Text("🌱", fontSize = 36.sp)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                SproutelleProgressIndicator(progress = 0.65f)
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    iconEmoji: String = "🌱",
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(iconEmoji, fontSize = 52.sp)
            Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = SproutelleColors.SecondaryText,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(SproutelleSpacing.Large))
                PrimaryButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SproutelleSpacing.XXLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = SproutelleColors.Error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(SproutelleSpacing.Large))
        PrimaryButton(
            text = "Retry",
            onClick = onRetryClick,
            modifier = Modifier.widthIn(max = 160.dp)
        )
    }
}
